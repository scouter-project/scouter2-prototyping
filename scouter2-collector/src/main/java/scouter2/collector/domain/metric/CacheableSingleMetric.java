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

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-18
 */
@AllArgsConstructor
@Getter
public class CacheableSingleMetric {
    long objId;
    String metricName;
    double value;
    long timestamp;
    long aliveMillis;

    public boolean isExpired(long now) {
        return now - timestamp > aliveMillis;
    }

    @Override
    public String toString() {
        return "CacheableSingleMetric{" +
                "objId=" + objId +
                ", metricName='" + metricName + '\'' +
                ", value=" + value +
                ", timestamp=" + timestamp +
                ", aliveMillis=" + aliveMillis +
                '}';
    }
}
