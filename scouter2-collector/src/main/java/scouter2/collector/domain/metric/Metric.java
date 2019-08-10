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

import com.google.protobuf.TextFormat;
import lombok.Getter;
import scouter2.collector.domain.instance.InstanceService;
import scouter2.proto.Metric4RepoP;
import scouter2.proto.MetricP;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
@Getter
public class Metric {
    MetricP proto;
    long timestamp;

    public Metric(MetricP proto, long timestamp) {
        this.proto = proto;
        this.timestamp = timestamp;
    }

    public Metric4RepoP toRepoType(long instanceId, MetricService metricService) {
        Metric4RepoP forRepo = Metric4RepoP.newBuilder()
                .setInstanceId(instanceId)
                .setMetricType(this.getProto().getMetricType())
                .build();

        forRepo.getMetricsMap().putAll(toEncodedKeyMetricsMap(this.getProto().getMetricsMap(), metricService));
        forRepo.getTagsMap().putAll(toEncodedKeyTagMap(this.getProto().getTagsMap(), metricService));

        return forRepo;
    }

    private long toInstanceId(String instanceFullName, InstanceService instanceService) {
        return instanceService.findIdByName(instanceFullName);
    }

    protected static Map<Long, Double> toEncodedKeyMetricsMap(Map<String, Double> metricsMap,
                                                     MetricService metricService) {

        Map<Long, Double> newMap = new HashMap<>();
        for (Map.Entry<String, Double> e : metricsMap.entrySet()) {
            newMap.put(metricService.findMetricIdAbsentGen(e.getKey()), e.getValue());
        }
        return newMap;
    }

    protected static Map<Long, String> toEncodedKeyTagMap(Map<String, String> tagsMap,
                                                                     MetricService metricService) {
        Map<Long, String> newMap = new HashMap<>();
        for (Map.Entry<String, String> e : tagsMap.entrySet()) {
            newMap.put(metricService.findTagIdAbsentGen(e.getKey()), e.getValue());
        }
        return newMap;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "proto=" + TextFormat.shortDebugString(proto) +
                '}';
    }
}