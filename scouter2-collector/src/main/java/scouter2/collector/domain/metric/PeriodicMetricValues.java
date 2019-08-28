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

import lombok.Getter;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-23
 */
@Getter
public class PeriodicMetricValues {
    long objId;
    String metricName;
    long[] timestamps;
    double[] values;
    int lastIndex;

    public PeriodicMetricValues(long objId, String metricName, int size) {
        this.objId = objId;
        this.metricName = metricName;
        timestamps = new long[size];
        values = new double[size];
        this.lastIndex = size - 1;
    }

    public void add(long timestamp, double value) {
        timestamps[lastIndex] = timestamp;
        values[lastIndex] = value;
    }
}
