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
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.common.util.RafUtil;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.infrastructure.db.PartitionKey;
import scouter2.collector.main.CoreRun;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.FileUtil;
import scouter2.proto.Metric4RepoP;
import scouter2.proto.TimeTypeP;

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
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("local")
@Slf4j
public class MetricFileDb {
    public static final ThrottleConfig S_0002 = ThrottleConfig.of("S0002");
    public static final ThrottleConfig S_0041 = ThrottleConfig.of("S0041");
    private static final long CLOSE_IDLE_MILLIS = 60000;

    private final Map<PartitionKey, Table> partitionTableMap = new ConcurrentHashMap<>();

    private ConfigCommon configCommon;

    public MetricFileDb(ConfigCommon configCommon) {
        this.configCommon = configCommon;
        ShutdownManager.getInstance().register(this::closeAll);
    }

    public long add(String pKey, Metric4RepoP metric) throws IOException {
        PartitionKey tableKey = new PartitionKey(pKey, metric.getTimeTypeValue());
        Table table = partitionTableMap.get(tableKey);
        if (table == null) {
            table = open(tableKey);
        }
        return table.add(metric);
    }

    public void readPeriod(String pKey, TimeTypeP timeTypeP, long startOffset, long toMillis,
                           Consumer<Metric4RepoP> consumer) throws IOException {

        PartitionKey tableKey = new PartitionKey(pKey, timeTypeP.getNumber());
        Table table = partitionTableMap.get(tableKey);
        if (table == null) {
            table = open(tableKey);
        }
        table.readPeriod(startOffset, toMillis, consumer);
    }

    private Table open(PartitionKey tableKey) throws FileNotFoundException {
        synchronized (partitionTableMap) {
            if (!CoreRun.isRunning()) {
                return new Table(new PartitionKey("temp", 0));
            }
            Table table = partitionTableMap.get(tableKey);
            if (table == null) {
                table = new Table(tableKey);
                partitionTableMap.put(tableKey, table);
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
            for (Map.Entry<PartitionKey, Table> entry : partitionTableMap.entrySet()) {
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
            for (Map.Entry<PartitionKey, Table> entry : partitionTableMap.entrySet()) {
                partitionTableMap.remove(entry.getKey());
                entry.getValue().close();
            }
        }
    }

    @Getter
    @Setter
    class Table {
        public static final String DB_NAME = "metric.filedb";

        int reference;
        PartitionKey tableKey;
        RandomAccessFile dataFile;
        RandomAccessFile dataFile4Read;
        long lastAccess;

        Table(PartitionKey tableKey) throws FileNotFoundException {
            this.tableKey = tableKey;
            String directory = configCommon.getDbDir() + "/" + tableKey.getPKey() + "/";
            FileUtil.mkdirs(directory);
            String dbFileName = directory + TimeTypeP.forNumber(tableKey.getTimeType()) + "_" + DB_NAME;
            this.dataFile = new RandomAccessFile(dbFileName, "rw");
            this.dataFile4Read = new RandomAccessFile(dbFileName, "r");
            this.lastAccess = U.now();
            log.info("[MetricFileDb.Table:{}] open.", tableKey.desc());
        }

        private void close() {
            log.info("[MetricFileDb.Table:{}] closing.", tableKey.desc());
            synchronized (partitionTableMap) {
                partitionTableMap.remove(tableKey);
            }
            FileUtil.close(dataFile);
            log.info("[MetricFileDb.Table:{}] closed.", tableKey.desc());
        }

        private long add(Metric4RepoP metric) throws IOException {
            if (metric == null) {
                return -1;
            }
            this.lastAccess = U.now();

            long offset = dataFile.length();
            dataFile.seek(offset);
            byte[] bytes = metric.toByteArray();
            dataFile.writeInt(bytes.length);
            dataFile.write(bytes);
            return offset;
        }

        private void readPeriod(long startOffset, long toMillis, Consumer<Metric4RepoP> consumer)
                throws IOException {

            this.lastAccess = U.now();

            long offset = startOffset;
            long loop = 0;

            while (true) {
                loop++;
                if (loop > 1000000) {
                    log.error("too many loops on xlog readPeriod()", S_0041);
                    break;
                }
                byte[] part = readOfSize(offset, 8 * 1024);
                long partSize = part.length;
                long partOffset = 0;

                if (partSize <= 4) {
                    if (partSize > 0) {
                        log.error("something wrong.(partSize is less than 4)", S_0002);
                    }
                    break;
                }

                DataInputStream data = new DataInputStream(new ByteArrayInputStream(part));
                while (true) {
                    if (partOffset + 4 >= partSize) {
                        break;
                    }

                    int size = data.readInt();
                    if (size == 0 || partOffset + 4 + size > partSize) {
                        break;
                    }

                    partOffset = partOffset + 4 + size;

                    byte[] buffer = new byte[size];
                    data.read(buffer);
                    Metric4RepoP metric = Metric4RepoP.parseFrom(buffer);
                    if (metric.getTimestamp() > toMillis) {
                        partOffset = (Long.MAX_VALUE) / 2L;
                        break;
                    }
                    consumer.accept(metric);
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
