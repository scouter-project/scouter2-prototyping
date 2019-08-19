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
import org.mapdb.Atomic;
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

import static scouter2.collector.infrastructure.mapdb.CommonDb.COMMON_DB_DIR;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
@Component
@RepoTypeMatch("local")
@Conditional(RepoTypeSelectorCondition.class)
@Slf4j
public class MetricDictDb implements Closeable {

    public static final String METRIC_DICT_DB = COMMON_DB_DIR + "metric_dict.db";

    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;
    private DB db;

    Atomic.Long metricKeyGenerator;
    private HTreeMap<Long, String> metricDict;
    private HTreeMap<Long, String> metricTagDict;
    private HTreeMap<String, Long> metricReverseDict;
    private HTreeMap<String, Long> metricTagReverseDict;

    public MetricDictDb(ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        FileUtil.mkdirs(configCommon.getDbDir());
        defineDb(configCommon);

        metricCollections();

        log.info("[MetricDictDb] open.");
        ShutdownManager.getInstance().register(this::close);
    }

    @Scheduled(fixedDelay = 500, initialDelay = 1000)
    public void schedule4CloseIdles() {
        if (CoreRun.isRunning() && !db.isClosed()) {
            db.commit();
        }
    }

    @Override
    public synchronized void close() {
        if (!db.isClosed()) {
            log.info("[MetricDictDb] closing.");
            db.commit();
            db.close();
        }
    }

    public void commit() {
        if (CoreRun.isRunning() && !db.isClosed()) {
            db.commit();
        }
    }

    private void defineDb(ConfigCommon configCommon) {
        FileUtil.mkdirs(configCommon.getDbDir() + COMMON_DB_DIR);
        db = DBMaker.fileDB(configCommon.getDbDir() + METRIC_DICT_DB)
                .fileChannelEnable()
                .fileMmapEnableIfSupported()
                .closeOnJvmShutdown()
                .transactionEnable()
                .checksumHeaderBypass()
                .allocateStartSize(1 * 1024 * 1024)
                .allocateIncrement(3 * 1024 * 1024)
                .make();
    }

    private void metricCollections() {
        metricKeyGenerator = db.atomicLong("metricKeyGenerator")
                .createOrOpen();

        metricDict = db.hashMap("metricDict")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.STRING)
                .counterEnable()
                .createOrOpen();

        metricTagDict = db.hashMap("metricTagDict")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.STRING)
                .counterEnable()
                .createOrOpen();

        metricReverseDict = db.hashMap("metricReverseDict")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG)
                .counterEnable()
                .createOrOpen();

        metricTagReverseDict = db.hashMap("metricTagReverseDict")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG)
                .counterEnable()
                .createOrOpen();
    }
}
