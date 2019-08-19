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

import scouter2.proto.TimeTypeP;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-18
 */
public class Metrics {

    public static long getAliveMillisOfType(TimeTypeP type) {
        if (type == TimeTypeP.REALTIME) {
            return 15 * 1000;
        } else if (type == TimeTypeP.ONE_MIN) {
            return 1 * 60 * 1000 + 5000;
        } else if (type == TimeTypeP.FIVE_MIN) {
            return 5 * 60 * 1000 + 5000;
        } else if (type == TimeTypeP.TEN_MIN) {
            return 10 * 60 * 1000 + 5000;
        } else if (type == TimeTypeP.DAY) {
            return 24 * 60 * 60 * 1000 + 5000;
        } else {
            return 12000;
        }
    }
}
