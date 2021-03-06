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

package scouter2.common.collection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-15
 */
public class LruMap {
    public static <K, V> Map<K, V> newOfMax(final int maxEntries) {

        LinkedHashMap<K, V> linkedHashMap = new LinkedHashMap<K, V>(maxEntries * 10 / 7, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                if (size() > maxEntries) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        return linkedHashMap;
    }
}
