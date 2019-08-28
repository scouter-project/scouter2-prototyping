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

package scouter2.fixture;

import scouter2.proto.Metric4RepoP;
import scouter2.proto.MetricTypeP;
import scouter2.proto.TimeTypeP;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-07
 */
public class Metric4RepoPFixture {

    public static Metric4RepoP getMetric4RepoP(long objId, long timestamp, double counterValue1,
                                               double counterValue2) {
        return Metric4RepoP.newBuilder()
                .setObjId(objId)
                .setMetricType(MetricTypeP.MEASURE)
                .setTimeType(TimeTypeP.REALTIME)
                .setTimestamp(timestamp)
                .putMetrics(1L, counterValue1)
                .putMetrics(2L, counterValue2)
                .build();
    }

    public static Metric4RepoP getCurrentAny() {
        return Metric4RepoP.newBuilder()
                .setObjId(1)
                .setMetricType(MetricTypeP.MEASURE)
                .setTimeType(TimeTypeP.REALTIME)
                .setTimestamp(System.currentTimeMillis())
                .putMetrics(1L, 100)
                .putMetrics(2L, 200)
                .build();
    }

    public static Metric4RepoP getAny(long objId, long timestamp) {
        return Metric4RepoP.newBuilder()
                .setObjId(objId)
                .setMetricType(MetricTypeP.MEASURE)
                .setTimeType(TimeTypeP.REALTIME)
                .setTimestamp(timestamp)
                .putMetrics(1L, 100)
                .putMetrics(2L, 200)
                .build();
    }

    public static Metric4RepoP getAny(long objId, long timestamp, TimeTypeP timeTypeP) {
        return Metric4RepoP.newBuilder()
                .setObjId(objId)
                .setMetricType(MetricTypeP.MEASURE)
                .setTimeType(timeTypeP)
                .setTimestamp(timestamp)
                .putMetrics(1L, 100)
                .putMetrics(2L, 200)
                .build();
    }
}