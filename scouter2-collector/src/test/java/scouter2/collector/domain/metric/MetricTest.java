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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import scouter2.proto.Metric4RepoP;
import scouter2.proto.MetricP;
import scouter2.fixture.MetricPFixture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-10
 */
public class MetricTest {

    @Mock
    MetricService metricService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void toRepoType() {
        int instanceId = 1000;
        String cpu = "Cpu";
        double cpuValue = 95.3;
        long cpuKey = 1;
        String mem = "Mem";
        double memValue = 33.5;
        long memKey = 2;
        MetricP proto = MetricPFixture.getCurrentWith(
                cpu, cpuValue,
                mem, memValue
        );
        long serverTimeStamp = System.currentTimeMillis() - 10 * 1000;
        Metric metric = new Metric(proto, serverTimeStamp);

        when(metricService.findMetricIdAbsentGen(cpu)).thenReturn(cpuKey);
        when(metricService.findMetricIdAbsentGen(mem)).thenReturn(memKey);

        //when
        Metric4RepoP actual = metric.toRepoType(instanceId, metricService, false);

        //then
        assertThat(actual.getObjId()).isEqualTo(instanceId);
        assertThat(actual.getMetricType()).isEqualTo(proto.getMetricType());
        assertThat(actual.getTimestamp()).isEqualTo(proto.getTimestamp());
        assertThat(actual.getMetricsOrThrow(cpuKey)).isEqualTo(proto.getMetricsOrThrow(cpu));
        assertThat(actual.getMetricsOrThrow(memKey)).isEqualTo(proto.getMetricsOrThrow(mem));
    }
}