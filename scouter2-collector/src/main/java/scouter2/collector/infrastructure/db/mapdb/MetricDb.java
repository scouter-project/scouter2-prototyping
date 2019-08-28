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
import scouter2.collector.infrastructure.db.PartitionKey;
import scouter2.collector.main.CoreRun;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.DateUtil;
import scouter2.proto.TimeTypeP;

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
public class MetricDb {

    private static final long AUTO_CLOSE_MILLIS = 60 * 1000;
    public static final String INDEX_NAME = "metric";

    public static final ThrottleConfig S_0038 = ThrottleConfig.of("S0038");

    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;

    private final ConcurrentHashMap<PartitionKey, WithTouch<DailyMinuteIndex>> minuteIndexMap = new ConcurrentHashMap<>();

    public MetricDb(ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        defineTodayDb();
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 12000)
    public void schedule4CloseIdles() {
        List<DailyMinuteIndex> candidates = new ArrayList<>();
        synchronized (minuteIndexMap) {
            for (Map.Entry<PartitionKey, WithTouch<DailyMinuteIndex>> e : minuteIndexMap.entrySet()) {
                if (e.getValue().isExpires() || e.getValue().getReal().getDb().isClosed()) {
                    candidates.add(e.getValue().getReal());
                }
            }
        }
        for (DailyMinuteIndex candidate : candidates) {
            try {
                candidate.close();
            } catch (Exception e) {
                log.error(e.getMessage(), S_0038, e);
            }
        }
        synchronized (minuteIndexMap) {
            for (Map.Entry<PartitionKey, WithTouch<DailyMinuteIndex>> e : minuteIndexMap.entrySet()) {
                if (e.getValue().getReal().getDb().isClosed()) {
                    minuteIndexMap.remove(e.getKey());
                }
            }
        }
    }

    public void closeAll() {
        synchronized (minuteIndexMap) {
            for (Map.Entry<PartitionKey, WithTouch<DailyMinuteIndex>> e : minuteIndexMap.entrySet()) {
                if (e.getValue().isExpires() || e.getValue().getReal().getDb().isClosed()) {
                    try {
                        e.getValue().getReal().close();
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), e);
                    }
                }
            }
        }
    }

    @Scheduled(fixedDelay = 500, initialDelay = 1000)
    public void schedule4Commit() {
        for (WithTouch<DailyMinuteIndex> value : minuteIndexMap.values()) {
            DailyMinuteIndex inner = value.inner;
            if (CoreRun.isRunning() && !inner.getDb().isClosed()) {
                inner.getDb().commit();
            }
        }
    }

    public DailyMinuteIndex getDailyIndex(String ymd, TimeTypeP timeTypeP) {
        PartitionKey partitionKey = new PartitionKey(ymd, timeTypeP.getNumber());
        WithTouch<DailyMinuteIndex> daily = minuteIndexMap.get(partitionKey);
        if (daily == null) {
            daily = defineDb(partitionKey);
        }
        daily.touch();

        return daily.getReal();
    }

    private WithTouch<DailyMinuteIndex> defineTodayDb() {
        String ymd = DateUtil.yyyymmdd(System.currentTimeMillis());
        return defineDb(new PartitionKey(ymd, TimeTypeP.REALTIME.getNumber()));
    }

    private WithTouch<DailyMinuteIndex> defineDb(PartitionKey partitionKey) {
        synchronized (minuteIndexMap) {
            if (minuteIndexMap.get(partitionKey) != null) {
                return minuteIndexMap.get(partitionKey);
            }
            DailyMinuteIndex todayIndex = new DailyMinuteIndex(INDEX_NAME, partitionKey, configCommon, configMapDb);
            WithTouch<DailyMinuteIndex> defined = new WithTouch<>(todayIndex, AUTO_CLOSE_MILLIS);
            minuteIndexMap.put(partitionKey, defined);
            return defined;
        }

    }
}
