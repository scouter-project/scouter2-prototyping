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

package scouter2.collector.transport.legacy;

import scouter.lang.pack.ObjectPack;
import scouter.lang.pack.PerfCounterPack;
import scouter.lang.value.Value;
import scouter2.proto.MetricP;
import scouter2.proto.ObjP;
import scouter2.proto.TimeTypeP;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-28
 */
public class LegacyMapper {

    public static ObjP toObjP(ObjectPack objectPack, String legacyFamily) {
        Map<String, String> tagMap = new HashMap<>();
        for (Map.Entry<String, Value> e : objectPack.tags.toMap().entrySet()) {
            tagMap.put(e.getKey(), e.getValue() == null ? "" : e.getValue().toString());
        }

        return ObjP.newBuilder()
                .setApplicationId(objectPack.objType)
                .setObjType(legacyFamily)
                .setObjLegacyType(objectPack.objType)
                .setObjFullName(objectPack.objName)
                .setLegacyObjHash(objectPack.objHash)
                .setAddress(objectPack.address)
                .setVersion(objectPack.version)
                .putAllTags(tagMap)
                .build();
    }

    public static MetricP toMetric(PerfCounterPack counterPack) {
        MetricP.Builder builder = MetricP.newBuilder()
                .setTimestamp(counterPack.time)
                .setObjFullName(counterPack.objName)
                .setTimeType(toTimeTypeP(counterPack.timetype));

        counterPack.data.toMap().forEach((key, value) -> builder.putMetrics(key, doubleValueOf(value)));

        return builder.build();
    }

    private static double doubleValueOf(Value value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0d;
    }

    public static TimeTypeP toTimeTypeP(byte legacyTimeType) {
        if (legacyTimeType == 1) {
            return TimeTypeP.REALTIME;
        }
        if (legacyTimeType == 2) {
            return TimeTypeP.ONE_MIN;
        }
        if (legacyTimeType == 3) {
            return TimeTypeP.FIVE_MIN;
        }
        if (legacyTimeType == 4) {
            return TimeTypeP.TEN_MIN;
        }
        if (legacyTimeType == 5) {
            return TimeTypeP.HOUR;
        }
        if (legacyTimeType == 6) {
            return TimeTypeP.DAY;
        }
        return TimeTypeP.REALTIME;
    }
}
