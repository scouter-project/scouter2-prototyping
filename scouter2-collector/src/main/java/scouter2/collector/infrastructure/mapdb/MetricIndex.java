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
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.DateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
@Component
@RepoTypeMatch("local")
@Conditional(RepoTypeSelectorCondition.class)
public class MetricIndex {

    private static final long AUTO_CLOSE_MILLIS = 15 * 1000;

    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;

    private final Map<String, WithTouch<MetricIndexDaily>> indexes = new HashMap<>();

    public MetricIndex(ConfigCommon configCommon, ConfigMapDb configMapDb) {
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;

        defineTodayIndex();
    }

    @Scheduled(fixedDelay = 3000, initialDelay = 15000)
    public void schedule4CloseIdles() {
        List<MetricIndexDaily> idles = new ArrayList<>();
        synchronized (indexes) {
            for (Map.Entry<String, WithTouch<MetricIndexDaily>> e : indexes.entrySet()) {
                if (e.getValue().isExpires()) {
                    indexes.remove(e.getKey());
                    idles.add(e.getValue().getReal());
                }
            }
        }
        for (MetricIndexDaily idle : idles) {
            idle.close();
        }
    }

    public MetricIndexDaily getDailyIndex(String ymd) {
        WithTouch<MetricIndexDaily> daily = indexes.get(ymd);
        if (daily == null) {
            daily = defineIndex(ymd);
        }
        daily.touch();

        return daily.getReal();
    }



    private WithTouch<MetricIndexDaily> defineTodayIndex() {
        String ymd = DateUtil.yyyymmdd(System.currentTimeMillis());
        return defineIndex(ymd);
    }

    private WithTouch<MetricIndexDaily> defineIndex(String ymd) {
        synchronized (indexes) {
            if (indexes.get(ymd) != null) {
                return indexes.get(ymd);
            }
            MetricIndexDaily todayIndex = new MetricIndexDaily(ymd, configCommon, configMapDb);
            WithTouch<MetricIndexDaily> defined = new WithTouch<>(todayIndex, AUTO_CLOSE_MILLIS);
            indexes.put(ymd, defined);
            return defined;
        }

    }
}
