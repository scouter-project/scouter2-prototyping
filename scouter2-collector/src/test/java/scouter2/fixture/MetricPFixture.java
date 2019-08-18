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

import scouter2.proto.MetricP;
import scouter2.proto.MetricTypeP;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-07
 */
public class MetricPFixture {

    public static MetricP getCurrentAny() {
        return MetricP.newBuilder()
                .setObjFullName("test/test")
                .setMetricType(MetricTypeP.MEASURE)
                .setTimestamp(System.currentTimeMillis())
                .putMetrics("Cpu", 99.5)
                .putMetrics("Mem", 55.0)
                .build();
    }

    public static MetricP getCurrentWith(String key1, double value1, String key2, double value2) {
        return MetricP.newBuilder()
                .setObjFullName("test/test")
                .setMetricType(MetricTypeP.MEASURE)
                .setTimestamp(System.currentTimeMillis())
                .putMetrics(key1, value1)
                .putMetrics(key2, value2)
                .build();
    }
}