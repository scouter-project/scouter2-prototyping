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
import scouter2.collector.common.util.U;
import scouter2.collector.domain.metric.Metric;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.FileUtil;
import scouter2.proto.Metric4RepoP;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public long add(String pKey, Metric4RepoP metric) throws FileNotFoundException {
        Table table = partitionTableMap.get(pKey);
        if (table == null) {
            table = open(pKey);
        }
        //todo
        return 0;
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

    /*
    TODO add shutdown hook
     */
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
        int reference;
        String pKey;
        RandomAccessFile dataFile;
        long lastAccess;

        Table(String pKey) throws FileNotFoundException {
            this.pKey = pKey;
            //TODO fileName
            String fileName = pKey + "";
            this.dataFile = new RandomAccessFile(fileName, "rw");
            this.lastAccess = U.now();
        }

        private void close() {
            partitionTableMap.remove(pKey);
            FileUtil.close(dataFile);
        }

        private long add(Metric metric) throws IOException {
            if (metric == null) {
                return 0;
            }

            synchronized (this) {

                long offset = dataFile.length();
                dataFile.seek(offset);
                dataFile.write(metric.getProto().toByteArray());
            }

            //TODO
            return 0;
        }
    }
}
