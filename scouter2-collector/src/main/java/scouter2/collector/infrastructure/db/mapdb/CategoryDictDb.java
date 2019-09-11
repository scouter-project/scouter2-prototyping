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
import org.mapdb.serializer.SerializerCompressionWrapper;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigCommon;
import scouter2.common.util.FileUtil;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
@Slf4j
public class CategoryDictDb implements Closeable {

    public static final String DB_DIR = "/common/";
    public final String INDEX_FILE;
    public static final ThrottleConfig S_0051 = ThrottleConfig.of("S0051");
    public static final ThrottleConfig S_0052 = ThrottleConfig.of("S0052");

    private String category;
    private int expireDaysAfterLastTouch;
    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;

    private DB db;

    private HTreeMap<Integer, String> dictMap;

    public CategoryDictDb(String category, int expireDaysAfterLastTouch, ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.INDEX_FILE = category + ".dict.db";
        this.category = category;
        this.expireDaysAfterLastTouch = expireDaysAfterLastTouch;
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        log.info("[CategoryDictDb][{}] trying open. ", category);

        FileUtil.mkdirs(configCommon.getDbDir());
        defineDb();

        log.info("[CategoryDictDb][{}] opened. ", category);
        ShutdownManager.getInstance().register(this::close);
    }

    @Override
    public synchronized void close() {
        if (!db.isClosed()) {
            log.info("[CategoryDictDb][{}] closing. ", category);
//            db.commit();
            db.close();
            log.info("[CategoryDictDb][{}] closed. ", category);
        } else {
            log.info("[CategoryDictDb][{}] already closed. ", category);
        }
    }

    private void defineDb() {
        String path = configCommon.getDbDir() + DB_DIR;
        FileUtil.mkdirs(path);
        try {
            db = DBMaker.fileDB(path + INDEX_FILE)
                    .fileChannelEnable()
                    .fileMmapEnableIfSupported()
                    .closeOnJvmShutdown()
//                    .transactionEnable()
                    .checksumHeaderBypass()
                    .allocateStartSize(3 * 1024 * 1024)
                    .allocateIncrement(10 * 1024 * 1024)
                    .make();

            DB.HashMapMaker<Integer, String> maker = db.hashMap("dictMap")
                    .keySerializer(Serializer.INTEGER)
                    .valueSerializer(new SerializerCompressionWrapper(Serializer.STRING));

            //All is Integer.MAX_VALUE now
            if (expireDaysAfterLastTouch != Integer.MAX_VALUE) {
                maker.expireAfterCreate(expireDaysAfterLastTouch, TimeUnit.DAYS)
                        .expireAfterGet(expireDaysAfterLastTouch, TimeUnit.DAYS);
            }
            this.dictMap = maker.createOrOpen();

        } catch (Exception e) {
            log.error(e.getMessage(), S_0051, e);
            try {
                db.close();
            } catch (Exception ex) {
                log.error(e.getMessage(), S_0052, e);
            }
        }
    }
}
