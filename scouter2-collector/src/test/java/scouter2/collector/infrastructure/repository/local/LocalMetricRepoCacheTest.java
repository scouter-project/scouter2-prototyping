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

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.primitive.LongSets;
import org.junit.Test;
import scouter2.collector.common.util.U;
import scouter2.collector.domain.metric.SingleMetric;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-18
 */
public class LocalMetricRepoCacheTest {

    public static final String APP_ID = "appId";

    @Test
    public void find_by_single_objId() {
        LocalMetricRepoCache cache = new LocalMetricRepoCache();
        long now = U.now();

        int expectedValue = 20;
        cache.addCurrentMetric(APP_ID, 1, 1, 10, now, 10000);
        cache.addCurrentMetric(APP_ID, 1, 1, expectedValue, now + 2000, 10000);
        cache.addCurrentMetric(APP_ID, 2, 1, 100, now, 10000);
        cache.addCurrentMetric(APP_ID, 2, 1, 200, now + 2000, 10000);

        MutableLongSet objIds = LongSets.mutable.of(1);
        MutableLongSet metricIds = LongSets.mutable.of(1);
        MutableList<SingleMetric> currentMetrics = cache.findCurrentMetrics(APP_ID, objIds, metricIds, now + 3000);

        assertThat(currentMetrics.size()).isEqualTo(1);
        assertThat(currentMetrics.get(0).getObjId()).isEqualTo(1);
        assertThat(currentMetrics.get(0).getMetricId()).isEqualTo(1);
        assertThat(currentMetrics.get(0).getValue()).isEqualTo(expectedValue);
    }

    @Test
    public void find_by_2_objIds() {
        LocalMetricRepoCache cache = new LocalMetricRepoCache();
        long now = U.now();

        int expectedValue1 = 20;
        int expectedValue2 = 200;
        cache.addCurrentMetric(APP_ID, 1, 100, 10, now, 10000);
        cache.addCurrentMetric(APP_ID, 1, 100, expectedValue1, now + 2000, 10000);
        cache.addCurrentMetric(APP_ID, 2, 100, 100, now, 10000);
        cache.addCurrentMetric(APP_ID, 2, 100, expectedValue2, now + 2000, 10000);
        cache.addCurrentMetric(APP_ID, 3, 100, 1000, now, 10000);
        cache.addCurrentMetric(APP_ID, 3, 100, 2000, now + 2000, 10000);

        MutableLongSet objIds = LongSets.mutable.of(1, 2);
        MutableLongSet metricIds = LongSets.mutable.of(100);
        MutableList<SingleMetric> currentMetrics = cache.findCurrentMetrics(APP_ID, objIds, metricIds, now + 3000);

        SingleMetric metric1 = currentMetrics.select(m -> m.getObjId() == 1).getFirst();
        SingleMetric metric2 = currentMetrics.select(m -> m.getObjId() == 2).getFirst();

        assertThat(currentMetrics.size()).isEqualTo(2);

        assertThat(metric1.getMetricId()).isEqualTo(100);
        assertThat(metric1.getValue()).isEqualTo(expectedValue1);
        assertThat(metric2.getMetricId()).isEqualTo(100);
        assertThat(metric2.getValue()).isEqualTo(expectedValue2);
    }

    @Test
    public void find_by_2_metrics() {
        LocalMetricRepoCache cache = new LocalMetricRepoCache();
        long now = U.now();

        int expectedValue1 = 20;
        int expectedValue2 = 200;
        cache.addCurrentMetric(APP_ID, 1, 100, 10, now, 10000);
        cache.addCurrentMetric(APP_ID, 1, 100, expectedValue1, now + 2000, 10000);
        cache.addCurrentMetric(APP_ID, 1, 200, 100, now, 10000);
        cache.addCurrentMetric(APP_ID, 1, 200, expectedValue2, now + 2000, 10000);
        cache.addCurrentMetric(APP_ID, 1, 300, 1000, now, 10000);
        cache.addCurrentMetric(APP_ID, 1, 300, 2000, now + 2000, 10000);

        MutableLongSet objIds = LongSets.mutable.of(1);
        MutableLongSet metricIds = LongSets.mutable.of(100, 200);
        MutableList<SingleMetric> currentMetrics = cache.findCurrentMetrics(APP_ID, objIds, metricIds, now + 3000);

        SingleMetric metric1 = currentMetrics.select(m -> m.getMetricId() == 100).getFirst();
        SingleMetric metric2 = currentMetrics.select(m -> m.getMetricId() == 200).getFirst();

        assertThat(currentMetrics.size()).isEqualTo(2);

        assertThat(metric1.getObjId()).isEqualTo(1);
        assertThat(metric1.getValue()).isEqualTo(expectedValue1);
        assertThat(metric2.getObjId()).isEqualTo(1);
        assertThat(metric2.getValue()).isEqualTo(expectedValue2);
    }

    @Test
    public void find_by_2_metrics_with_2_objs() {
        LocalMetricRepoCache cache = new LocalMetricRepoCache();
        long now = U.now();

        int expectedValue1 = 20;
        int expectedValue2 = 200;
        cache.addCurrentMetric(APP_ID, 1, 100, 10, now, 10000);
        cache.addCurrentMetric(APP_ID, 1, 100, expectedValue1, now + 2000, 10000);
        cache.addCurrentMetric(APP_ID, 1, 200, 100, now, 10000);
        cache.addCurrentMetric(APP_ID, 1, 200, expectedValue2, now + 2000, 10000);
        cache.addCurrentMetric(APP_ID, 1, 300, 1000, now, 10000);
        cache.addCurrentMetric(APP_ID, 1, 300, 2000, now + 2000, 10000);

        cache.addCurrentMetric(APP_ID, 2, 100, 10, now, 10000);
        cache.addCurrentMetric(APP_ID, 2, 100, expectedValue1, now + 2000, 10000);
        cache.addCurrentMetric(APP_ID, 2, 200, 100, now, 10000);
        cache.addCurrentMetric(APP_ID, 2, 200, expectedValue2, now + 2000, 10000);

        cache.addCurrentMetric(APP_ID, 3, 100, 10, now, 10000);
        cache.addCurrentMetric(APP_ID, 3, 100, 20, now + 2000, 10000);

        MutableLongSet objIds = LongSets.mutable.of(1, 2);
        MutableLongSet metricIds = LongSets.mutable.of(100, 200);
        MutableList<SingleMetric> currentMetrics = cache.findCurrentMetrics(APP_ID, objIds, metricIds, now + 3000);

        SingleMetric metric100_obj1 = currentMetrics
                .select(m -> m.getMetricId() == 100 && m.getObjId() == 1).getFirst();
        SingleMetric metric200_obj1 = currentMetrics
                .select(m -> m.getMetricId() == 200 && m.getObjId() == 1).getFirst();
        SingleMetric metric100_obj2 = currentMetrics
                .select(m -> m.getMetricId() == 100 && m.getObjId() == 2).getFirst();
        SingleMetric metric200_obj2 = currentMetrics
                .select(m -> m.getMetricId() == 200 && m.getObjId() == 2).getFirst();

        assertThat(currentMetrics.size()).isEqualTo(4);

        assertThat(metric100_obj1.getValue()).isEqualTo(expectedValue1);
        assertThat(metric200_obj1.getValue()).isEqualTo(expectedValue2);
        assertThat(metric100_obj2.getValue()).isEqualTo(expectedValue1);
        assertThat(metric200_obj2.getValue()).isEqualTo(expectedValue2);
    }

    @Test
    public void find_with_ignoring_expired() {
        LocalMetricRepoCache cache = new LocalMetricRepoCache();
        long now = U.now();

        int expectedValue = 20;
        cache.addCurrentMetric(APP_ID, 1, 1, expectedValue, now + 5000, 10000);
        cache.addCurrentMetric(APP_ID, 2, 1, 200, now, 10000);

        MutableLongSet objIds = LongSets.mutable.of(1, 2);
        MutableLongSet metricIds = LongSets.mutable.of(1);
        MutableList<SingleMetric> currentMetrics = cache.findCurrentMetrics(APP_ID, objIds, metricIds, now + 11000);

        assertThat(currentMetrics.size()).isEqualTo(1);
        assertThat(currentMetrics.get(0).getObjId()).isEqualTo(1);
        assertThat(currentMetrics.get(0).getMetricId()).isEqualTo(1);
        assertThat(currentMetrics.get(0).getValue()).isEqualTo(expectedValue);
    }
}