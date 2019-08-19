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

package scouter2.collector.infrastructure.mapdb;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.main.CoreRun;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.FileUtil;

import java.io.Closeable;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
@Component
@RepoTypeMatch("local")
@Conditional(RepoTypeSelectorCondition.class)
@Slf4j
public class CommonDb implements Closeable {

    public static final String COMMON_DB_DIR = "/common/";
    public static final String SYS_PROPS_DB = COMMON_DB_DIR + "sys_prop.db";

    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;
    private DB db;

    private HTreeMap<String, String> commonPropMap;

    public CommonDb(ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        FileUtil.mkdirs(configCommon.getDbDir());
        defineDb(configCommon);

        commonCollections();

        log.info("[CommonDb] open.");
        ShutdownManager.getInstance().register(this::close);
    }

    @Scheduled(fixedDelay = 500, initialDelay = 1000)
    public void schedule4CloseIdles() {
        if (CoreRun.isRunning() && !db.isClosed()) {
            db.commit();
        }
    }

    public void commit() {
        if (CoreRun.isRunning() && !db.isClosed()) {
            db.commit();
        }
    }

    @Override
    public synchronized void close() {
        if (!db.isClosed()) {
            log.info("[CommonDb] closing.");
            db.commit();
            db.close();
        }
    }

    private void defineDb(ConfigCommon configCommon) {
        FileUtil.mkdirs(configCommon.getDbDir() + COMMON_DB_DIR);
        db = DBMaker.fileDB(configCommon.getDbDir() + SYS_PROPS_DB)
                .fileChannelEnable()
                .fileMmapEnableIfSupported()
                .closeOnJvmShutdown()
                .transactionEnable()
                .checksumHeaderBypass()
                .allocateStartSize(1 * 1024 * 1024)
                .allocateIncrement(3 * 1024 * 1024)
                .make();
    }

    private void commonCollections() {
        commonPropMap = db.hashMap("commonProp")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.STRING)
                .counterEnable()
                .createOrOpen();
    }
}
