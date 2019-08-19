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

package scouter2.collector.infrastructure.repository.local;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.LongObjectMaps;
import org.springframework.stereotype.Component;
import scouter2.collector.domain.metric.SingleMetric;
import scouter2.common.collection.LruMap;

import java.util.Map;
import java.util.Objects;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-18
 */
@Slf4j
@Component
public class LocalMetricRepoCache {

    @Getter
    private static class CurrentMetric {
        String applicationId;
        MutableLongObjectMap<MutableLongObjectMap<SingleMetric>> metrics = LongObjectMaps.mutable.empty();

        CurrentMetric(String applicationId) {
            this.applicationId = applicationId;
        }

        void add(long objId, long metricId, double value, long timestamp, long aliveMillis) {
            SingleMetric metricOfObj = new SingleMetric(objId, metricId, value, timestamp, aliveMillis);
            MutableLongObjectMap<SingleMetric> longKeyMap = metrics.getIfAbsentPut(metricId, LongObjectMaps.mutable::empty);

            longKeyMap.put(objId, metricOfObj);
        }
    }


    private Map<String, CurrentMetric> currentMetricCache = LruMap.newOfMax(1000);

    public void addCurrentMetric(String applicationId, long objId, long metricId, double value, long timestamp,
                                 long aliveMillis) {

        CurrentMetric currentByApplication =
                currentMetricCache.computeIfAbsent(applicationId, k -> new CurrentMetric(applicationId));

        currentByApplication.add(objId, metricId, value, timestamp, aliveMillis);
    }

    public MutableList<SingleMetric> findCurrentMetrics(String applicationId, LongSet objIds, LongSet metricIds,
                                                        long now) {

        MutableList<SingleMetric> allMetricList = Lists.mutable.empty();
        CurrentMetric currentMetric = currentMetricCache.get(applicationId);
        if (currentMetric == null) {
            return allMetricList;
        }

        MutableLongObjectMap<MutableLongObjectMap<SingleMetric>> byApplication = currentMetric.getMetrics();

        metricIds.collect(byApplication::get).select(Objects::nonNull).forEach(mapByObjId -> {
            MutableList<SingleMetric> metricList = objIds
                    .collect(mapByObjId::get)
                    .select(Objects::nonNull)
                    .rejectWith(SingleMetric::isExpired, now).toList();

            allMetricList.addAll(metricList);
        });

        return allMetricList;
    }
}
