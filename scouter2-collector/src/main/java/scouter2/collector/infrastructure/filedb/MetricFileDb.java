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

package scouter2.collector.infrastructure.filedb;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.common.util.RafUtil;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.FileUtil;
import scouter2.proto.Metric4RepoP;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("local")
@Slf4j
public class MetricFileDb {
    private static final long CLOSE_IDLE_MILLIS = 10000;

    private final Map<String, Table> partitionTableMap = new HashMap<>();

    private ConfigCommon configCommon;

    public MetricFileDb(ConfigCommon configCommon) {
        this.configCommon = configCommon;
        ShutdownManager.getInstance().register(this::closeAll);
    }

    public long add(String pKey, Metric4RepoP metric) throws IOException {
        Table table = partitionTableMap.get(pKey);
        if (table == null) {
            table = open(pKey);
        }
        return table.add(metric);
    }

    public void readPeriod(String pKey, long startOffset, long toMillis, Consumer<Metric4RepoP> metric4RepoPConsumer)
            throws IOException {

        Table table = partitionTableMap.get(pKey);
        if (table == null) {
            table = open(pKey);
        }
        table.readPeriod(startOffset, toMillis, metric4RepoPConsumer);
    }

    private Table open(String pKey) throws FileNotFoundException {
        synchronized (partitionTableMap) {
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
        public static final String DB_NAME = "metric.filedb";

        int reference;
        String pKey;
        RandomAccessFile dataFile;
        long lastAccess;

        Table(String pKey) throws FileNotFoundException {
            this.pKey = pKey;
            String directory = configCommon.getDbDir() + "/" + pKey + "/";
            FileUtil.mkdirs(directory);
            String dbFileName = directory + DB_NAME;
            this.dataFile = new RandomAccessFile(dbFileName, "rw");
            this.lastAccess = U.now();
        }

        private void close() {
            partitionTableMap.remove(pKey);
            FileUtil.close(dataFile);
        }

        private long add(Metric4RepoP metric) throws IOException {
            if (metric == null) {
                return -1;
            }
            synchronized (this) {
                long offset = dataFile.length();
                dataFile.seek(offset);
                byte[] bytes = metric.toByteArray();
                dataFile.writeInt(bytes.length);
                dataFile.write(bytes);
                return offset;
            }
        }

        private void readPeriod(long startOffset, long toMillis, Consumer<Metric4RepoP> metric4RepoPConsumer)
                throws IOException {

            long offset = startOffset;
            long loop = 0;
            while (true) {
                loop++;
                if (loop > 1000000) {
                    //to many loops
                    break;
                }
                byte[] part = readOfSize(offset, 4 * 1024);
                long partSize = part.length;
                long partOffset = 0;

                if (partSize <= 4) {
                    if (partSize > 0) {
                        log.error("something wrong.(partSize is less than 4)");
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
                    metric4RepoPConsumer.accept(metric);
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