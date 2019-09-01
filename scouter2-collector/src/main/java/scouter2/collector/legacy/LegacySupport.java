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

package scouter2.collector.legacy;

import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import scouter.lang.value.ListValue;
import scouter2.proto.ObjP;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-15
 */
public class LegacySupport {

    public static final String APPLICATION_ID_FOR_SCOUTER1_AGENT = "SCOUTER1-DEFAULT";
    public static final String DUMMY_OBJ_FULL_NAME_FOR_SCOUTER1_AGENT = "dummyObjForScouter1";

    /**
     * Some legacy transport protocol doesn't have obj information,
     * but scouter2 must have obj info on all transport protocol.
     * (actually the applicationId is mandatory for persistence layer for partitioning)
     * In these case, dummy obj is treated as default value.
     */
    public static ObjP getDummyObjP() {
        return ObjP.newBuilder()
                .setObjFullName(DUMMY_OBJ_FULL_NAME_FOR_SCOUTER1_AGENT)
                .setObjLegacyType("javaee")
                .setObjFamily("javaee")
                .setVersion("0.0.0")
                .setAddress("127.0.0.1")
                .setApplicationId(APPLICATION_ID_FOR_SCOUTER1_AGENT)
                .build();
    }

    public static MutableLongList listValue2LongList(ListValue objHashParamLv) {
        MutableLongList objIds = LongLists.mutable.empty();
        for (int i = 0; i < objHashParamLv.size(); i++) {
            objIds.add(objHashParamLv.getInt(i));
        }
        return objIds;
    }
}
