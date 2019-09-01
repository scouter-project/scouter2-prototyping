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

package scouter2.collector.domain.metric;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.springframework.stereotype.Component;
import scouter2.proto.Metric4RepoP;
import scouter2.proto.TimeTypeP;

import java.util.function.Consumer;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-05
 */
@Component
@Slf4j
public class MetricService {
    private static MetricService instance;

    private MetricRepo repo;
    private MetricServiceCache cache;

    public MetricService(MetricRepo repo, MetricServiceCache cache) {
        synchronized (MetricService.class) {
            if (instance != null) {
                throw new IllegalStateException();
            }
            this.repo = repo;
            this.cache = cache;
            instance = this;
        }
    }

    public static MetricService getInstance() {
        return instance;
    }

    public long findMetricIdAbsentGen(String metricName) {
        return cache.findMetricIdAbsentGen(metricName);
    }

    public String findMetricNameById(long metricId) {
        return cache.findMetricNameById(metricId);
    }

    public MutableList<SingleMetricDatum> findCurrentMetricData(String applicationId,
                                                                LongSet objIds,
                                                                MutableSet<String> metricNames) {

        MutableLongSet metricIds = metricNames.collectLong(this::findMetricIdAbsentGen).toSet();
        return repo.findCurrentMetrics(applicationId, objIds, metricIds);
    }

    public MutableList<CacheableSingleMetric> findCurrentMetrics(String applicationId,
                                                                 LongSet objIds,
                                                                 MutableSet<String> metricNames) {

        MutableLongSet metricIds = metricNames.collectLong(this::findMetricIdAbsentGen).toSet();
        MutableList<SingleMetricDatum> data = repo.findCurrentMetrics(applicationId, objIds, metricIds);

        return data.collectWith(SingleMetricDatum::toDomain, this::findMetricNameById);
    }

    public void streamListByObjs(String applicationId, LongSet objIds, long from, long to,
                                 TimeTypeP timeType, Consumer<Metric4RepoP> stream) {

        repo.streamByObjs(applicationId, objIds, timeType, from, to, stream);
    }

}
