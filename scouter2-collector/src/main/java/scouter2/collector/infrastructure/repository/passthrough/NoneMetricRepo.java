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

package scouter2.collector.infrastructure.repository.passthrough;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.impl.factory.Lists;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.metric.MetricRepo;
import scouter2.collector.domain.metric.MetricRepoAdapter;
import scouter2.collector.domain.metric.SingleMetricDatum;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.proto.Metric4RepoP;
import scouter2.proto.TimeTypeP;

import java.util.function.Consumer;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 09/09/2019
 */
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("none")
public class NoneMetricRepo extends MetricRepoAdapter implements MetricRepo, NonThreadSafeRepo {
    @Override
    public String getRepoType() {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void add(String applicationId, Metric4RepoP metric) {
    }

    @Override
    public void stream(String applicationId, TimeTypeP timeType, long from, long to, Consumer<Metric4RepoP> stream) {
    }

    @Override
    public void streamByObjs(String applicationId, LongSet objIds, TimeTypeP timeType, long from, long to, Consumer<Metric4RepoP> stream) {
    }

    @Override
    public long findMetricIdAbsentGen(String metricName) {
        return 0;
    }

    @Override
    public String findMetricNameById(long metricId) {
        return null;
    }

    @Override
    public MutableList<SingleMetricDatum> findCurrentMetrics(String applicationId, LongSet objIds, LongSet metricIds) {
        return Lists.mutable.empty();
    }
}
