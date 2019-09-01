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

package scouter2.collector.domain.dict;

import scouter.util.IntKeyLinkedMap;
import scouter.util.IntLongLinkedMap;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigDict;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 01/09/2019
 */
public class DictCache {
    private static long expireCacheMillis = 1 * 60 * 60 * 1000L;

    String applicationId;
    Map<String, IntKeyLinkedMap<String>> dictCaches = new ConcurrentHashMap<>();
    Map<String, IntLongLinkedMap> touchMaps = new ConcurrentHashMap<>();

    public DictCache(String applicationId, ConfigDict configDict) {
        this.applicationId = applicationId;
        for (DictCategory category : DictCategory.values()) {
            int cacheSize = category.getCacheHint() * configDict.getDictCacheMultiplier();
            IntKeyLinkedMap<String> map = new IntKeyLinkedMap<String>().setMax(cacheSize);
            dictCaches.put(category.getCategory(), new IntKeyLinkedMap<String>().setMax(cacheSize));
            touchMaps.put(category.getCategory(), new IntLongLinkedMap().setMax(cacheSize));
        }
    }

    public String get(String category, int hash) {
        IntKeyLinkedMap<String> cache = dictCaches.get(category);
        if (cache == null) {
            return null;
        }
        String text = cache.get(hash);
        if (text == null) {
            return null;
        }
        IntLongLinkedMap touchMap = touchMaps.get(category);
        if (touchMap.get(hash) + expireCacheMillis < U.now()) {
            return null;
        }
        return text;
    }

    public void put(String category, int hash, String text) {
        IntKeyLinkedMap<String> cache = dictCaches.get(category);
        IntLongLinkedMap touchMap = touchMaps.get(category);
        if (cache != null) {
            cache.put(hash, text);
            touchMap.put(hash, U.now());
        }
    }

    public void putIfAbsent(String category, int hash, String text) {
        if (get(category, hash) == null) {
            put(category, hash, text);
        }
    }
}
