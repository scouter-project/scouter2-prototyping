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

import lombok.extern.slf4j.Slf4j;
import org.mapdb.HTreeMap;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.domain.metric.MetricRepo;
import scouter2.collector.infrastructure.filedb.MetricFileDb;
import scouter2.collector.infrastructure.mapdb.CommonDb;
import scouter2.collector.infrastructure.mapdb.MetricIndex;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.util.DateUtil;
import scouter2.proto.Metric4RepoP;

import java.io.FileNotFoundException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-17
 */
@Slf4j
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("local")
public class LocalMetricRepo extends LocalRepoAdapter implements MetricRepo {

    private CommonDb commonDb;
    private MetricFileDb metricFileDb;
    private MetricIndex metricIndex;

    public LocalMetricRepo(CommonDb commonDb, MetricFileDb metricFileDb, MetricIndex metricIndex) {
        this.commonDb = commonDb;
        this.metricFileDb = metricFileDb;
        this.metricIndex = metricIndex;
    }

    @Override
    public void add(Metric4RepoP metric) {
        try {
            String ymd = DateUtil.yyyymmdd(metric.getTimestamp());
            long offset = metricFileDb.add(ymd, metric);

            HTreeMap<Long, Long> metricMinIndex = metricIndex.getDailyIndex(ymd).getMetricMinIndex();

            long min = DateUtil.getMinUnit(metric.getTimestamp());
            if (!metricMinIndex.containsKey(min)) {
                metricMinIndex.put(min, offset);

                HTreeMap<Long, Long> metricHourIndex = metricIndex.getDailyIndex(ymd).getMetricHourIndex();
                long hour = DateUtil.getHourUnit(metric.getTimestamp());
                metricHourIndex.putIfAbsent(hour, min);
            }

        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public long findMetricIdAbsentGen(String metricName) {
        return commonDb.getMetricReverseDict()
                .computeIfAbsent(metricName,
                        k -> commonDb.getMetricKeyGenerator().incrementAndGet());
    }

    @Override
    public long findTagIdAbsentGen(String tagName) {
        return commonDb.getMetricTagReverseDict()
                .computeIfAbsent(tagName,
                        k -> commonDb.getMetricTagKeyGenerator().incrementAndGet());
    }
}
