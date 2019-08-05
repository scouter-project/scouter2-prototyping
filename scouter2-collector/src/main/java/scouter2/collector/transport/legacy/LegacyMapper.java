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
import scouter.lang.value.Value;
import scouter2.proto.InstanceP;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-28
 */
public class LegacyMapper {
    public static InstanceP toInstance(ObjectPack objectPack, String legacyFamily) {
        Map<String, String> tagMap = new HashMap<>();
        for (Map.Entry<String, Value> e : objectPack.tags.toMap().entrySet()) {
            tagMap.put(e.getKey(), e.getValue() == null ? "" : e.getValue().toString());
        }

        return InstanceP.newBuilder()
                .setApplicationId(objectPack.objType)
                .setInstanceType(legacyFamily)
                .setInstanceLegacyType(objectPack.objType)
                .setInstanceFullName(objectPack.objName)
                .setLegacyInstanceHash(objectPack.objHash)
                .setAddress(objectPack.address)
                .setVersion(objectPack.version)
                .putAllTags(tagMap)
                .build();
    }
}
