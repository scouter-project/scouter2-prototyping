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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.main.CoreRun;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
@Slf4j
@Component
@RepoTypeMatch("local")
@Conditional(RepoTypeSelectorCondition.class)
public class XlogDb {

    private static final long AUTO_CLOSE_MILLIS = 60 * 1000;
    public static final String INDEX_NAME = "xlog";

    public static final ThrottleConfig S_0038 = ThrottleConfig.of("S0038");

    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;

    private final ConcurrentHashMap<String, WithTouch<XlogIndexes>> indexMap = new ConcurrentHashMap<>();

    public XlogDb(ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        defineTodayDb();
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 12000)
    public void schedule4CloseIdles() {
        List<XlogIndexes> candidates = new ArrayList<>();
        synchronized (indexMap) {
            for (Map.Entry<String, WithTouch<XlogIndexes>> e : indexMap.entrySet()) {
                if (e.getValue().isExpires()
                        || e.getValue().getReal().getPeriodicIndex().getDb().isClosed()
                        || e.getValue().getReal().getIdIndex().getDb().isClosed()
                ) {
                    candidates.add(e.getValue().getReal());
                }
            }
        }
        for (XlogIndexes candidate : candidates) {
            try {
                candidate.close();
            } catch (Exception e) {
                log.error(e.getMessage(), S_0038, e);
            }
        }
        synchronized (indexMap) {
            for (Map.Entry<String, WithTouch<XlogIndexes>> e : indexMap.entrySet()) {
                if (e.getValue().getReal().getPeriodicIndex().getDb().isClosed()
                        || e.getValue().getReal().getIdIndex().getDb().isClosed()) {
                    indexMap.remove(e.getKey());
                }
            }
        }
    }

    @Scheduled(fixedDelay = 500, initialDelay = 1000)
    public void schedule4Commit() {
        for (WithTouch<XlogIndexes> value : indexMap.values()) {
            XlogIndexes inner = value.inner;
            if (CoreRun.isRunning()) {
                inner.commit();
            }
        }
    }

    public DailySecondIndex getPeriodicIndex(String dayKey) {
        WithTouch<XlogIndexes> daily = touchAndGet(dayKey);
        return daily.getReal().getPeriodicIndex();
    }

    public XlogIdIndex getIdIndex(String dayKey) {
        WithTouch<XlogIndexes> daily = touchAndGet(dayKey);
        return daily.getReal().getIdIndex();
    }

    @NotNull
    private WithTouch<XlogIndexes> touchAndGet(String dayKey) {
        WithTouch<XlogIndexes> daily = indexMap.get(dayKey);
        if (daily == null) {
            daily = defineDb(dayKey);
        }
        daily.touch();
        return daily;
    }

    private WithTouch<XlogIndexes> defineTodayDb() {
        String dayKey = DateUtil.yyyymmdd(System.currentTimeMillis());
        return defineDb(dayKey);
    }

    private WithTouch<XlogIndexes> defineDb(String dayKey) {
        synchronized (indexMap) {
            if (indexMap.get(dayKey) != null) {
                return indexMap.get(dayKey);
            }
            DailySecondIndex periodicIndex = new DailySecondIndex(INDEX_NAME, dayKey, configCommon, configMapDb);
            XlogIdIndex idIndex = new XlogIdIndex(dayKey, configCommon, configMapDb);

            XlogIndexes indexes = new XlogIndexes(periodicIndex, idIndex);

            WithTouch<XlogIndexes> defined = new WithTouch<>(indexes, AUTO_CLOSE_MILLIS);
            indexMap.put(dayKey, defined);
            return defined;
        }
    }

    @Getter
    @AllArgsConstructor
    private static class XlogIndexes {
        DailySecondIndex periodicIndex;
        XlogIdIndex idIndex;

        public void commit() {
            try {
                if (!periodicIndex.getDb().isClosed()) {
                    periodicIndex.getDb().commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (!idIndex.getDb().isClosed()) {
                    idIndex.getDb().commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void close() {
            try {
                periodicIndex.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                idIndex.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
