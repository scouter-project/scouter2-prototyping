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
import org.jetbrains.annotations.NotNull;
import org.mapdb.HTreeMap;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.main.CoreRun;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.collector.springconfig.ThreadNameDecorator;
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
public class ProfileDb {

    private static final long AUTO_CLOSE_MILLIS = 60 * 1000;
    public static final String INDEX_NAME = "profile";

    public static final ThrottleConfig S_0060 = ThrottleConfig.of("S0060");

    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;

    private final ConcurrentHashMap<String, WithTouch<ProfileIndex>> indexMap = new ConcurrentHashMap<>();

    public ProfileDb(ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        defineTodayDb();
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 12000)
    public void schedule4CloseIdles() {
        ThreadNameDecorator.runWithName(this.getClass().getSimpleName(), () -> {
            List<ProfileIndex> candidates = new ArrayList<>();
            synchronized (indexMap) {
                for (Map.Entry<String, WithTouch<ProfileIndex>> e : indexMap.entrySet()) {
                    if (e.getValue().isExpires() || e.getValue().getReal().getDb().isClosed()) {
                        candidates.add(e.getValue().getReal());
                    }
                }
            }
            for (ProfileIndex candidate : candidates) {
                try {
                    candidate.close();
                } catch (Exception e) {
                    log.error(e.getMessage(), S_0060, e);
                }
            }
            synchronized (indexMap) {
                for (Map.Entry<String, WithTouch<ProfileIndex>> e : indexMap.entrySet()) {
                    if (e.getValue().getReal().getDb().isClosed()) {
                        indexMap.remove(e.getKey());
                    }
                }
            }
        });
    }

//    @Scheduled(fixedDelay = 500, initialDelay = 1000)
    public void schedule4Commit() {
        for (WithTouch<ProfileIndex> value : indexMap.values()) {
            ProfileIndex inner = value.inner;
            if (CoreRun.isRunning()) {
//                inner.commit();
            }
        }
    }

    public HTreeMap<byte[], LongList> getIndex(String dayKey) {
        WithTouch<ProfileIndex> daily = touchAndGet(dayKey);
        return daily.getReal().getProfileOffsetIndex();
    }

    @NotNull
    private WithTouch<ProfileIndex> touchAndGet(String dayKey) {
        WithTouch<ProfileIndex> daily = indexMap.get(dayKey);
        if (daily == null) {
            daily = defineDb(dayKey);
        }
        daily.touch();
        return daily;
    }

    private WithTouch<ProfileIndex> defineTodayDb() {
        String dayKey = DateUtil.yyyymmdd(System.currentTimeMillis());
        return defineDb(dayKey);
    }

    private WithTouch<ProfileIndex> defineDb(String dayKey) {
        synchronized (indexMap) {
            if (indexMap.get(dayKey) != null) {
                return indexMap.get(dayKey);
            }
            ProfileIndex profileIndex = new ProfileIndex(dayKey, configCommon, configMapDb);

            WithTouch<ProfileIndex> defined = new WithTouch<>(profileIndex, AUTO_CLOSE_MILLIS);
            indexMap.put(dayKey, defined);
            return defined;
        }
    }
}
