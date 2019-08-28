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
 * @author Gun Lee (gunlee01@gmail.com) on 24/08/2019
 */
public class TimeTypeSupport {

    public static long truncate4TimeType(TimeTypeP timeTypeP, long timestamp) {
        if (timeTypeP == TimeTypeP.REALTIME) {
            return timestamp;

        } else if (timeTypeP == TimeTypeP.FIVE_MIN) {
            return timestamp / 300000 * 300000;

        } else if (timeTypeP == TimeTypeP.HOUR) {
            return timestamp / 3600000 * 3600000;

        } else if (timeTypeP == TimeTypeP.DAY) {
            return timestamp / 86400000 * 86400000;

        } else if (timeTypeP == TimeTypeP.ONE_MIN) {
            return timestamp / 60000 * 60000;

        } else if (timeTypeP == TimeTypeP.TEN_MIN) {
            return timestamp / 600000 * 600000;
        }

        return timestamp;
    }
}
