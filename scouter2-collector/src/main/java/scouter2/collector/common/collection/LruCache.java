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

package scouter2.collector.common.collection;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-15
 */
public class LruCache<K, V> {
    private final String name;
    private final ConcurrentHashMap<K, V> access;
    private final LinkedHashMap<K, V> creation;

    public LruCache(int maxEntries, String name) {
        this.name = name;
        this.access = new ConcurrentHashMap<>(maxEntries * 10 / 7, 0.7f);
        this.creation = new LinkedHashMap<K, V>(maxEntries * 10 / 7, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                if (size() > maxEntries) {
                    access.remove(eldest.getKey());
                    //TODO remove it
                    System.out.println("LruMap maxEntries are reached. " + maxEntries + ", " + eldest.getKey());
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    public V putIfAbsent(K key, V backup) {
        V value = this.access.get(key);
        if (value == null) {
            synchronized (this.creation) {
                value = this.creation.get(key);
                if (value == null) {
                    value = backup;
                    this.access.put(key, value);
                    this.creation.put(key, value);
                }
            }
        }
        return value;
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V value = this.access.get(key);
        if (value == null) {
            synchronized (this.creation) {
                value = this.creation.get(key);
                if (value == null) {
                    value = mappingFunction.apply(key);
                    this.access.put(key, value);
                    this.creation.put(key, value);
                }
            }
        }
        return value;
    }


    public V get(K key) {
        return this.access.get(key);
    }

    public void put(K key, V value) {
        synchronized (this.creation) {
            this.access.put(key, value);
            this.creation.put(key, value);
        }
    }

    public void remove(K key) {
        synchronized (this.creation) {
            this.access.remove(key);
            this.creation.remove(key);
        }
    }

    public MutableList<V> values() {
        MutableList<V> values = Lists.mutable.empty();
        Enumeration<V> elements = this.access.elements();
        while (elements.hasMoreElements()) {
            values.add(elements.nextElement());
        }
        return values;
    }

    public String name() {
        return name;
    }
}
