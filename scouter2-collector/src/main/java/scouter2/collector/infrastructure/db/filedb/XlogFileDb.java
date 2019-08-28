/*
 * Copyright 2019. The Scouter2 Authors.
 *
 *  @https://github.com/scouter-project/scouter2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scouter2.collector.infrastructure.db.filedb;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.common.util.RafUtil;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.domain.xlog.Xlog;
import scouter2.collector.main.CoreRun;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.DateUtil;
import scouter2.common.util.FileUtil;
import scouter2.proto.XlogP;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-27
 */
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("local")
@Slf4j
public class XlogFileDb {
    public static final ThrottleConfig S_0039 = ThrottleConfig.of("S0039");
    public static final ThrottleConfig S_0040 = ThrottleConfig.of("S0040");
    private static final long CLOSE_IDLE_MILLIS = 60000;

    private final Map<String, Table> partitionTableMap = new ConcurrentHashMap<>();

    private ConfigCommon configCommon;

    public XlogFileDb(ConfigCommon configCommon) {
        this.configCommon = configCommon;
        ShutdownManager.getInstance().register(this::closeAll);
    }

    public long add(String pKey, Xlog xlog) throws IOException {
        Table table = partitionTableMap.get(pKey);
        if (table == null) {
            table = open(pKey);
        }
        return table.add(xlog);
    }

    public void readPeriod(String pKey, long startOffset, long toMillis,
                           LongSet objIds, Consumer<XlogP> xlogPConsumer) throws IOException {

        Table table = partitionTableMap.get(pKey);
        if (table == null) {
            table = open(pKey);
        }
        table.readPeriod(startOffset, toMillis, objIds, xlogPConsumer);
    }

    private Table open(String pKey) throws FileNotFoundException {
        synchronized (partitionTableMap) {
            if (!CoreRun.isRunning()) {
                return new Table("temp");
            }
            Table table = partitionTableMap.get(pKey);
            if (table == null) {
                table = new Table(pKey);
                partitionTableMap.put(pKey, table);
            }
            return table;
        }
    }

    @Scheduled(fixedDelay = 15000, initialDelay = 15000)
    public void schedule4CloseIdles() {
        closeIdles();
    }

    private void closeIdles() {
        long now = U.now();
        List<Table> idles = new ArrayList<>();

        synchronized (partitionTableMap) {
            for (Map.Entry<String, Table> entry : partitionTableMap.entrySet()) {
                if (entry.getValue().lastAccess < now - CLOSE_IDLE_MILLIS) {
                    partitionTableMap.remove(entry.getKey());
                    idles.add(entry.getValue());
                }
            }
        }

        for (Table idle : idles) {
            idle.close();
        }
    }

    private void closeAll() {
        synchronized (partitionTableMap) {
            for (Map.Entry<String, Table> entry : partitionTableMap.entrySet()) {
                partitionTableMap.remove(entry.getKey());
                entry.getValue().close();
            }
        }
    }

    @Getter
    @Setter
    class Table {
        public static final String DB_NAME = "xlog.filedb";

        int reference;
        String pKey;
        RandomAccessFile dataFile;
        RandomAccessFile dataFile4Read;
        long lastAccess;

        Table(String pKey) throws FileNotFoundException {
            this.pKey = pKey;
            String directory = configCommon.getDbDir() + "/" + pKey + "/";
            FileUtil.mkdirs(directory);
            String dbFileName = directory + DB_NAME;
            this.dataFile = new RandomAccessFile(dbFileName, "rw");
            this.dataFile4Read = new RandomAccessFile(dbFileName, "r");
            this.lastAccess = U.now();
            log.info("[XlogFileDb.Table:{}] open.", pKey);
        }

        private void close() {
            log.info("[XlogFileDb.Table:{}] closing.", pKey);
            synchronized (partitionTableMap) {
                partitionTableMap.remove(pKey);
            }
            FileUtil.close(dataFile);
            log.info("[XlogFileDb.Table:{}] closed.", pKey);
        }

        private long add(Xlog xlog) throws IOException {
            if (xlog == null) {
                return -1;
            }
            this.lastAccess = U.now();
            long offset = dataFile.length();
            dataFile.seek(offset);
            byte[] bytes = xlog.getProto().toByteArray();

            DataOutputX out = new DataOutputX();
            out.writeInt(DateUtil.getSecUnit(xlog.getTimestamp()));
            out.writeDecimal(xlog.getProto().getObjId());
            out.writeInt(bytes.length);
            out.write(bytes);
            dataFile.write(out.toByteArray());

            return offset;
        }

        private void readPeriod(long startOffset, long toMillis, LongSet objIds, Consumer<XlogP> xlogPConsumer)
                throws IOException {

            this.lastAccess = U.now();

            long offset = startOffset;
            long loop = 0;
            while (true) {
                loop++;
                if (loop > 1000000) {
                    log.error("too many loops on xlog readPeriod()", S_0040);
                    break;
                }
                byte[] part = readOfSize(offset, 32 * 1024);
                long partSize = part.length;
                long partOffset = 0;

                if (partSize <=  5 + 8 + 8) {
                    if (partSize > 0) {
                        log.error("something wrong.(partSize is less than 21)", S_0039);
                    }
                    break;
                }

                DataInputX dix = new DataInputX(new DataInputStream(new ByteArrayInputStream(part)));
                while (true) {
                    if (partOffset + 5 + 8 + 8 >= partSize) {
                        break;
                    }

                    int startDixOffset = dix.getOffset();

                    long endTime = DateUtil.reverseSecUnit(dix.readInt());
                    long objId = dix.readDecimal();
                    int size = dix.readInt();
                    if (size == 0 || dix.getOffset() + size > partSize) {
                        break;
                    }

                    partOffset += (dix.getOffset() + size - startDixOffset);
                    if (!objIds.contains(objId)) {
                        continue;
                    }
                    if (endTime > toMillis) {
                        partOffset = (Long.MAX_VALUE) / 2L;
                        break;
                    }

                    byte[] buffer = dix.read(size);
                    XlogP xlog = XlogP.parseFrom(buffer);
                    xlogPConsumer.accept(xlog);
                }

                offset += partOffset;
            }
        }

        private byte[] readOfSize(long startOffset, int size) throws IOException {
            synchronized (this) {
                return RafUtil.readOfSize(dataFile, startOffset, size);
            }
        }
    }
}
