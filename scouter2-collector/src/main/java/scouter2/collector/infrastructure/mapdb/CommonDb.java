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
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.FileUtil;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
@Component
@RepoTypeMatch("local")
@Conditional(RepoTypeSelectorCondition.class)
public class CommonDb {

    public static final String SYS_PROPS_DB_DIR = "/common/";
    public static final String SYS_PROPS_DB = SYS_PROPS_DB_DIR + "sysProps.db";
    public static final String COUNTER_INDEX = "/%s/counter.idx";

    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;
    private DB sysPropDb;

    private HTreeMap<String, String> commonPropMap;

    Atomic.Long instanceIdGenerator;
    private HTreeMap<Long, byte[]> instanceMap;
    private HTreeMap<String, Long> instanceNameIdMap;

    Atomic.Long metricKeyGenerator;
    Atomic.Long metricTagKeyGenerator;
    private HTreeMap<Long, String> metricDict;
    private HTreeMap<Long, String> metricTagDict;
    private HTreeMap<String, Long> metricReverseDict;
    private HTreeMap<String, Long> metricTagReverseDict;

    public CommonDb(ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        FileUtil.mkdirs(configCommon.getDbDir());
        defineSysPropDb(configCommon);

        commonCollections();
        instanceCollections();
        metricCollections();
    }

    private void defineSysPropDb(ConfigCommon configCommon) {
        FileUtil.mkdirs(configCommon.getDbDir() + SYS_PROPS_DB_DIR);
        sysPropDb = DBMaker.fileDB(configCommon.getDbDir() + SYS_PROPS_DB)
                .fileChannelEnable()
                .fileMmapEnableIfSupported()
                .closeOnJvmShutdown()
                .transactionEnable()
                .allocateStartSize(1 * 1024 * 1024)
                .allocateIncrement(5 * 1024 * 1024)
                .make();
    }

    private void metricCollections() {
        metricDict = sysPropDb.hashMap("metricDict")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.STRING)
                .counterEnable()
                .createOrOpen();

        metricTagDict = sysPropDb.hashMap("metricTagDict")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.STRING)
                .counterEnable()
                .createOrOpen();

        metricReverseDict = sysPropDb.hashMap("metricReverseDict")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG)
                .counterEnable()
                .createOrOpen();

        metricTagReverseDict = sysPropDb.hashMap("metricTagReverseDict")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG)
                .counterEnable()
                .createOrOpen();
    }

    private void instanceCollections() {
        instanceMap = sysPropDb.hashMap("instance")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.BYTE_ARRAY)
                .createOrOpen();

        instanceNameIdMap = sysPropDb.hashMap("instanceNameId")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG)
                .createOrOpen();

        instanceIdGenerator = sysPropDb.atomicLong("instanceId")
                .createOrOpen();
    }

    private void commonCollections() {
        commonPropMap = sysPropDb.hashMap("commonProp")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.STRING)
                .createOrOpen();
    }
}
