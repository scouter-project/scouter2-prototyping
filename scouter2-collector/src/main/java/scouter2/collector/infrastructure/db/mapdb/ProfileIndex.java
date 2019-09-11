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
import org.eclipse.collections.api.list.primitive.LongList;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.main.CoreRun;
import scouter2.common.util.FileUtil;

import java.io.Closeable;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
@Slf4j
public class ProfileIndex implements Closeable {

    public static final String DB_DIR = "/%s/";
    public final String INDEX_FILE;
    public static final ThrottleConfig S_0058 = ThrottleConfig.of("S0058");
    public static final ThrottleConfig S_0059 = ThrottleConfig.of("S0059");

    private String dayKey;
    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;

    private DB db;

    private HTreeMap<byte[], LongList> profileOffsetIndex;

    public ProfileIndex(String dayKey,
                        ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.INDEX_FILE = "profile.idx";
        this.dayKey = dayKey;
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        log.info("[ProfileIndex] trying open. {}", dayKey);

        FileUtil.mkdirs(configCommon.getDbDir());
        defineDayIndex();

        log.info("[ProfileIndex] opened. {}", dayKey);
        ShutdownManager.getInstance().register(this::close);
    }

    public void commit() {
        if (CoreRun.isRunning() && !db.isClosed()) {
//            db.commit();
        }
    }

    @Override
    public synchronized void close() {
        if (!db.isClosed()) {
            log.info("[ProfileIndex] closing. {}", dayKey);
//            db.commit();
            db.close();
            log.info("[ProfileIndex] closed. {}", dayKey);
        } else {
            log.info("[ProfileIndex] already closed. {}", dayKey);
        }
    }

    private void defineDayIndex() {
        String path = configCommon.getDbDir() + String.format(DB_DIR, dayKey);
        FileUtil.mkdirs(path);
        try {
            db = DBMaker.fileDB(path + INDEX_FILE)
                    .fileChannelEnable()
                    .fileMmapEnableIfSupported()
                    .closeOnJvmShutdown()
//                    .transactionEnable()
                    .checksumHeaderBypass()
                    .allocateStartSize(1 * 1024 * 1024)
                    .allocateIncrement(3 * 1024 * 1024)
                    .make();

            profileOffsetIndex = db.hashMap("gxidIndex")
                    .keySerializer(Serializer.BYTE_ARRAY)
                    .valueSerializer(new MapDbObjectSerializer())
                    .createOrOpen();

        } catch (Exception e) {
            log.error(e.getMessage(), S_0058, e);
            try {
                db.close();
            } catch (Exception ex) {
                log.error(e.getMessage(), S_0059, e);
            }
        }
    }
}
