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

package scouter2.collector.infrastructure.db.mapdb;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.infrastructure.db.PartitionKey;
import scouter2.collector.infrastructure.db.filedb.HourUnitWithMinutes;
import scouter2.common.util.FileUtil;
import scouter2.proto.TimeTypeP;

import java.io.Closeable;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
@Slf4j
public class DailyMinuteIndex implements Closeable {

    public static final String DB_DIR = "/%s/";
    public final String INDEX_FILE;
    public static final ThrottleConfig S_0045 = ThrottleConfig.of("S0045");
    public static final ThrottleConfig S_0046 = ThrottleConfig.of("S0046");

    private String name;
    private PartitionKey dayKey;
    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;

    private DB db;

    private HTreeMap<Long, HourUnitWithMinutes> hourUnitMap;
    private HTreeMap<Long, Long> minuteIndex;

    public DailyMinuteIndex(String name, PartitionKey dayKey,
                            ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.INDEX_FILE = "%s_" + name + ".m.idx";
        this.name = name;
        this.dayKey = dayKey;
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        log.info("[DailyMinuteIndex][{}] trying open. {}_{}",
                name, dayKey.getPKey(), TimeTypeP.forNumber(dayKey.getTimeType()));

        FileUtil.mkdirs(configCommon.getDbDir());
        defineDayIndex();

        log.info("[DailyMinuteIndex][{}] opened. {}_{}",
                name, dayKey.getPKey(), TimeTypeP.forNumber(dayKey.getTimeType()));
        ShutdownManager.getInstance().register(this::close);
    }

    @Override
    public synchronized void close() {
        if (!db.isClosed()) {
            log.info("[DailyMinuteIndex][{}] closing. {}_{}",
                    name, dayKey.getPKey(), TimeTypeP.forNumber(dayKey.getTimeType()));
//            db.commit();
            db.close();
            log.info("[DailyMinuteIndex][{}] closed. {}_{}",
                    name, dayKey.getPKey(), TimeTypeP.forNumber(dayKey.getTimeType()));
        } else {
            log.info("[DailyMinuteIndex][{}] already closed. {}_{}",
                    name, dayKey.getPKey(), TimeTypeP.forNumber(dayKey.getTimeType()));
        }
    }

    private void defineDayIndex() {
        String path = configCommon.getDbDir() + String.format(DB_DIR, dayKey.getPKey());
        FileUtil.mkdirs(path);
        try {
            db = DBMaker.fileDB(path + String.format(INDEX_FILE, TimeTypeP.forNumber(dayKey.getTimeType())))
                    .fileChannelEnable()
                    .fileMmapEnableIfSupported()
                    .closeOnJvmShutdown()
//                    .transactionEnable()
                    .checksumHeaderBypass()
                    .allocateStartSize(1 * 1024 * 1024)
                    .allocateIncrement(3 * 1024 * 1024)
                    .make();

            hourUnitMap = db.hashMap("hourUnitMap")
                    .keySerializer(Serializer.LONG)
                    .valueSerializer(new MapDbObjectSerializer())
                    .createOrOpen();

            minuteIndex = db.hashMap("minuteIndex")
                    .keySerializer(Serializer.LONG)
                    .valueSerializer(Serializer.LONG)
                    .createOrOpen();

        } catch (Exception e) {
            log.error(e.getMessage(), S_0045, e);
            try {
                db.close();
            } catch (Exception ex) {
                log.error(e.getMessage(), S_0046, e);
            }
        }
    }
}
