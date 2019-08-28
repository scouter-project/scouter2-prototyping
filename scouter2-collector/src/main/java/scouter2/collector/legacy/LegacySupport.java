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

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-15
 */
public class LegacySupport {

    public static final String APPLICATION_ID_FOR_SCOUTER1_AGENT = "SCOUTER1-DEFAULT";

    public static MutableLongList listValue2LongList(ListValue objHashParamLv) {
        MutableLongList objIds = LongLists.mutable.empty();
        for (int i = 0; i < objHashParamLv.size(); i++) {
            objIds.add(objHashParamLv.getInt(i));
        }
        return objIds;
    }
}
