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
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.main.CoreRun;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.collector.springconfig.ThreadNameDecorator;
import scouter2.common.util.FileUtil;
import scouter2.proto.ProfileP;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-27
 */
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("local")
@Slf4j
public class ProfileFileDb {
    public static final ThrottleConfig S_0056 = ThrottleConfig.of("S0056");
    public static final ThrottleConfig S_0057 = ThrottleConfig.of("S0057");
    private static final long CLOSE_IDLE_MILLIS = 60000;

    private final Map<String, Table> partitionTableMap = new ConcurrentHashMap<>();

    private ConfigCommon configCommon;

    public ProfileFileDb(ConfigCommon configCommon) {
        this.configCommon = configCommon;
        ShutdownManager.getInstance().register(this::closeAll);
    }

    @Scheduled(fixedDelay = 15000, initialDelay = 15000)
    public void schedule4CloseIdles() {
        ThreadNameDecorator.runWithName(this.getClass().getSimpleName(), () -> {
            closeIdles();
        });
    }

    public long add(String pKey, ProfileP profileP) throws IOException {
        Table table = partitionTableMap.get(pKey);
        if (table == null) {
            table = open(pKey);
        }
        return table.add(profileP);
    }

    public ProfileP get(String pKey, long offset) throws IOException {
        Table table = partitionTableMap.get(pKey);
        if (table == null) {
            table = open(pKey);
        }
        return table.get(offset);
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
        public static final String DB_NAME = "profile.filedb";

        int reference;
        String pKey;
        final RandomAccessFile dataFile;
        final RandomAccessFile dataFile4Read;
        long offset;
        long lastAccess;

        Table(String pKey) throws FileNotFoundException {
            this.pKey = pKey;
            String directory = configCommon.getDbDir() + "/" + pKey + "/";
            FileUtil.mkdirs(directory);
            String dbFileName = directory + DB_NAME;
            this.dataFile = new RandomAccessFile(dbFileName, "rw");
            this.dataFile4Read = new RandomAccessFile(dbFileName, "r");
            this.lastAccess = U.now();
            log.info("[ProfileFileDb.Table:{}] open.", pKey);
        }

        private void close() {
            log.info("[ProfileFileDb.Table:{}] closing.", pKey);
            synchronized (partitionTableMap) {
                partitionTableMap.remove(pKey);
            }
            FileUtil.close(dataFile);
            FileUtil.close(dataFile4Read);
            log.info("[ProfileFileDb.Table:{}] closed.", pKey);
        }

        private long add(ProfileP profileP) throws IOException {
            if (profileP == null || profileP.getSteps() == null || profileP.getSteps().size() == 0) {
                return -1;
            }
            this.lastAccess = U.now();
            long offset = dataFile.length();
            dataFile.seek(offset);
            byte[] bytes = profileP.toByteArray();

            dataFile.writeInt(bytes.length);
            dataFile.write(bytes);

            this.offset = offset;
            return offset;
        }

        private ProfileP get(long offset) throws IOException {
            if (offset < 0) {
                return null;
            }
            byte[] protoBytes;
            synchronized (dataFile4Read) {
                dataFile4Read.seek(offset);
                int length = dataFile4Read.readInt();
                protoBytes = new byte[length];
                dataFile4Read.read(protoBytes);
            }
            return ProfileP.parseFrom(protoBytes);
        }
    }
}
