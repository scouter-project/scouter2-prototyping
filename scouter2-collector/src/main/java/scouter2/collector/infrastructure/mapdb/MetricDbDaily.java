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
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.infrastructure.filedb.HourUnitWithMinutes;
import scouter2.common.util.FileUtil;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
public class MetricDbDaily {

    public static final String METRIC_INDEX_DIR = "/%s/";
    public static final String METRIC_INDEX_FILE = "metric.idx";

    private String ymd;
    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;

    private DB db;

    private HTreeMap<Long, HourUnitWithMinutes> hourUnitMap;
    private HTreeMap<Long, Long> minuteIndex;

    public MetricDbDaily(String ymd, ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.ymd = ymd;
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        FileUtil.mkdirs(configCommon.getDbDir());
        defineDayIndex(ymd);
    }

    public void close() {
        hourUnitMap.close();
        minuteIndex.close();
        db.close();
    }

    private void defineDayIndex(String ymd) {
        String path = configCommon.getDbDir() + String.format(METRIC_INDEX_DIR, ymd);
        FileUtil.mkdirs(path);
        db = DBMaker.fileDB(String.format(path + METRIC_INDEX_FILE, this.ymd))
                .fileChannelEnable()
                .fileMmapEnableIfSupported()
                .closeOnJvmShutdown()
                .transactionEnable()
                .allocateStartSize(1 * 1024 * 1024)
                .allocateIncrement(3 * 1024 * 1024)
                .make();

        hourUnitMap = db.hashMap("hourUnitMap")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.ELSA)
                .createOrOpen();

        minuteIndex = db.hashMap("minuteIndex")
                .keySerializer(Serializer.LONG)
                .valueSerializer(Serializer.LONG)
                .createOrOpen();
        
    }
}
