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

package scouter2.collector.sandbox;

import lombok.Getter;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import scouter2.collector.domain.obj.Obj;
import scouter2.common.util.FileUtil;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
public class CommonDb4Test {

    public static final String SYS_PROPS_DB_DIR = "/common/";
    public static final String SYS_PROPS_DB = SYS_PROPS_DB_DIR + "sysProps.db";
    public static final String COUNTER_INDEX = "/%s/counter.idx";

    private String dbDir;
    private DB db;

    private HTreeMap<String, String> commonPropMap;

    Atomic.Long objIdGenerator;
    private HTreeMap<Long, Obj> objMap;
    private HTreeMap<String, Long> objNameIdMap;

    Atomic.Long metricKeyGenerator;
    private HTreeMap<Long, String> metricDict;
    private HTreeMap<Long, String> metricTagDict;
    private HTreeMap<String, Long> metricReverseDict;
    private HTreeMap<String, Long> metricTagReverseDict;

    public CommonDb4Test(String dbDir) {
        this.dbDir = dbDir;
        FileUtil.mkdirs(dbDir);
        defineSysPropDb();

        commonCollections();
        objCollections();
        metricCollections();
    }

    private void defineSysPropDb() {
        FileUtil.mkdirs(dbDir + SYS_PROPS_DB_DIR);
        db = DBMaker.fileDB(dbDir + SYS_PROPS_DB)
                .fileChannelEnable()
                .fileMmapEnableIfSupported()
                .closeOnJvmShutdown()
                .transactionEnable()
                .checksumHeaderBypass()
                .allocateStartSize(1 * 1024 * 1024)
                .allocateIncrement(5 * 1024 * 1024)
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

    private void objCollections() {
        objMap = db.hashMap("obj")
                .keySerializer(Serializer.LONG)
//                .valueSerializer(new MapDbObjectSerializer())
                .valueSerializer(Serializer.ELSA)
                .counterEnable()
                .createOrOpen();

        objNameIdMap = db.hashMap("objNameId")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.LONG)
                .counterEnable()
                .createOrOpen();

        objIdGenerator = db.atomicLong("objId")
                .createOrOpen();
    }

    private void commonCollections() {
        commonPropMap = db.hashMap("commonProp")
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.STRING)
                .counterEnable()
                .createOrOpen();
    }
}
