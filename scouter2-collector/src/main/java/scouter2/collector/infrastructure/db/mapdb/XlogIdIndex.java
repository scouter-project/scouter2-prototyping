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
import org.eclipse.collections.api.list.MutableList;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigCommon;
import scouter2.common.util.FileUtil;

import java.io.Closeable;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
@Slf4j
public class XlogIdIndex implements Closeable {

    public static final String DB_DIR = "/%s/";
    public final String INDEX_FILE;
    public static final ThrottleConfig S_0053 = ThrottleConfig.of("S0053");
    public static final ThrottleConfig S_0054 = ThrottleConfig.of("S0054");

    private String dayKey;
    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;

    private DB db;

    private HTreeMap<byte[], Long> txidIndex;
    private HTreeMap<byte[], MutableList<byte[]>> gxidIndex;

    public XlogIdIndex(String dayKey,
                       ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.INDEX_FILE = "xlog.id.idx";
        this.dayKey = dayKey;
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        log.info("[XlogIdIndex] trying open. {}", dayKey);

        FileUtil.mkdirs(configCommon.getDbDir());
        defineDayIndex();

        log.info("[XlogIdIndex] opened. {}", dayKey);
        ShutdownManager.getInstance().register(this::close);
    }

    @Override
    public synchronized void close() {
        if (!db.isClosed()) {
            log.info("[XlogIdIndex] closing. {}", dayKey);
//            db.commit();
            db.close();
            log.info("[XlogIdIndex] closed. {}", dayKey);
        } else {
            log.info("[XlogIdIndex] already closed. {}", dayKey);
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

            txidIndex = db.hashMap("txidIndex")
                    .keySerializer(Serializer.BYTE_ARRAY)
                    .valueSerializer(Serializer.LONG)
                    .createOrOpen();

            gxidIndex = db.hashMap("gxidIndex")
                    .keySerializer(Serializer.BYTE_ARRAY)
                    .valueSerializer(new MapDbObjectSerializer())
                    .createOrOpen();

        } catch (Exception e) {
            log.error(e.getMessage(), S_0053, e);
            try {
                db.close();
            } catch (Exception ex) {
                log.error(e.getMessage(), S_0054, e);
            }
        }
    }
}
