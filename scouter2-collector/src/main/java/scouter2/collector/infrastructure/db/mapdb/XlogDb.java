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

    private final ConcurrentHashMap<String, WithTouch<DailySecondIndex>> secondIndexMap = new ConcurrentHashMap<>();

    public XlogDb(ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        defineTodayDb();
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 12000)
    public void schedule4CloseIdles() {
        List<DailySecondIndex> candidates = new ArrayList<>();
        synchronized (secondIndexMap) {
            for (Map.Entry<String, WithTouch<DailySecondIndex>> e : secondIndexMap.entrySet()) {
                if (e.getValue().isExpires() || e.getValue().getReal().getDb().isClosed()) {
                    candidates.add(e.getValue().getReal());
                }
            }
        }
        for (DailySecondIndex candidate : candidates) {
            try {
                candidate.close();
            } catch (Exception e) {
                log.error(e.getMessage(), S_0038, e);
            }
        }
        synchronized (secondIndexMap) {
            for (Map.Entry<String, WithTouch<DailySecondIndex>> e : secondIndexMap.entrySet()) {
                if (e.getValue().getReal().getDb().isClosed()) {
                    secondIndexMap.remove(e.getKey());
                }
            }
        }
    }

    @Scheduled(fixedDelay = 500, initialDelay = 1000)
    public void schedule4Commit() {
        for (WithTouch<DailySecondIndex> value : secondIndexMap.values()) {
            DailySecondIndex inner = value.inner;
            if (CoreRun.isRunning() && !inner.getDb().isClosed()) {
                inner.getDb().commit();
            }
        }
    }

    public DailySecondIndex getDailyPeriodicIndex(String dayKey) {
        WithTouch<DailySecondIndex> daily = secondIndexMap.get(dayKey);
        if (daily == null) {
            daily = defineDb(dayKey);
        }
        daily.touch();

        return daily.getReal();
    }

    private WithTouch<DailySecondIndex> defineTodayDb() {
        String dayKey = DateUtil.yyyymmdd(System.currentTimeMillis());
        return defineDb(dayKey);
    }

    private WithTouch<DailySecondIndex> defineDb(String dayKey) {
        synchronized (secondIndexMap) {
            if (secondIndexMap.get(dayKey) != null) {
                return secondIndexMap.get(dayKey);
            }
            DailySecondIndex todayIndex = new DailySecondIndex(INDEX_NAME, dayKey, configCommon, configMapDb);
            WithTouch<DailySecondIndex> defined = new WithTouch<>(todayIndex, AUTO_CLOSE_MILLIS);
            secondIndexMap.put(dayKey, defined);
            return defined;
        }
    }
}
