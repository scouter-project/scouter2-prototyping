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

import org.eclipse.collections.api.map.primitive.MutableLongLongMap;
import org.eclipse.collections.api.map.primitive.MutableObjectLongMap;
import org.eclipse.collections.api.tuple.primitive.LongIntPair;
import org.eclipse.collections.impl.factory.primitive.LongLongMaps;
import org.eclipse.collections.impl.factory.primitive.ObjectLongMaps;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import org.springframework.stereotype.Component;
import scouter2.collector.config.ConfigMetric;
import scouter2.collector.domain.obj.ObjService;
import scouter2.proto.MetricP;
import scouter2.proto.TimeTypeP;

import static scouter2.collector.domain.metric.TimeTypeSupport.truncate4TimeType;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Component
public class MetricAdder {

    ConfigMetric conf;
    MetricRepoQueue repoQueue;
    ObjService objService;
    MutableLongLongMap nonRealTimeMetricTimeWindows = LongLongMaps.mutable.empty();
    MutableObjectLongMap<LongIntPair> autoSamplingTimeWindows = ObjectLongMaps.mutable.empty();
    MutableLongLongMap autoSamplingCheckerTimeWindows = LongLongMaps.mutable.empty();

    public MetricAdder(ConfigMetric conf, MetricRepoQueue repoQueue, ObjService objService) {
        this.conf = conf;
        this.repoQueue = repoQueue;
        this.objService = objService;
    }

    public void addMetric(Metric metric) {
        MetricP proto = metric.getProto();
        Long objID = objService.findIdByName(proto.getObjFullName());
        if (objID == null) return;

        long objId = objID;
        if (proto.getTimeType() == TimeTypeP.REALTIME) {
            repoQueue.offer(metric);
            sampling5min(metric, objId);

        } else {
            addNonRealtimeMetric(metric, objId);
        }
    }

    private void addNonRealtimeMetric(Metric metric, long objId) {
        long tsTruncated = truncate4TimeType(metric.getProto().getTimeType(), metric.getTimestamp());
        if (isNewTimeWindowOfNonRealtimeMetric(objId, tsTruncated)) {
            nonRealTimeMetricTimeWindows.put(objId, tsTruncated);
            repoQueue.offer(metric);
        }
    }

    private boolean isNewTimeWindowOfNonRealtimeMetric(long objId, long tsTruncated) {
        long lastTimestamp = nonRealTimeMetricTimeWindows.get(objId);
        return lastTimestamp == 0 || lastTimestamp != tsTruncated;
    }

    private void sampling5min(Metric metric, long objId) {
        if (nonRealTimeMetricTimeWindows.contains(objId)) {
            return;
        }

        MetricP proto = metric.getProto();
        long truncated30sec = metric.getTimestamp() / 30000 * 30000;
        if (isBeforePrevCheckInterval(objId, truncated30sec)) {
            return;
        }
        autoSamplingCheckerTimeWindows.put(objId, truncated30sec);

        LongIntPair objAndMetric = PrimitiveTuples.pair(objId, proto.getMetricsMap().keySet().hashCode());
        long truncated5min = truncate4TimeType(TimeTypeP.FIVE_MIN, metric.getTimestamp());
        if (hasTheSameTimeWindowsSampling(objAndMetric, truncated5min)) {
            return;
        }
        autoSamplingTimeWindows.put(objAndMetric, truncated5min);
        repoQueue.offer(metric.withTimeTypeAdvised(TimeTypeP.FIVE_MIN));
    }

    private boolean isBeforePrevCheckInterval(long objId, long truncated30sec) {
        long lastChecked = autoSamplingCheckerTimeWindows.get(objId);
        return lastChecked == truncated30sec;
    }

    private boolean hasTheSameTimeWindowsSampling(LongIntPair key, long truncated5min) {
        long lastSampled = autoSamplingTimeWindows.get(key);
        return lastSampled == truncated5min;
    }
}
