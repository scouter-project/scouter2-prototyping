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

package scouter2.collector.springconfig;

import net.sf.ehcache.Element;
import net.sf.ehcache.ElementIdHelper;
import net.sf.ehcache.store.compound.ReadWriteCopyStrategy;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import scouter2.collector.common.kryo.KryoSupport;
import scouter2.collector.domain.obj.Obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-23
 */
public class ScouterCacheReadWriteSerializationCopyStrategy implements ReadWriteCopyStrategy<Element> {

    private static final long serialVersionUID = 683291039483472136L;

    private KryoSupport kryoSupport;

    public ScouterCacheReadWriteSerializationCopyStrategy() {
        List<Pair<Class<?>, Integer>> classAndIdList = new ArrayList<>();
        classAndIdList.add(Tuples.pair(TreeSet.class, 21));
        classAndIdList.add(Tuples.pair(MutableList.class, 22));
        classAndIdList.add(Tuples.pair(ImmutableList.class, 23));
        classAndIdList.add(Tuples.pair(Obj.class, 23));
        classAndIdList.add(Tuples.pair(HashMap.class, 24));

        kryoSupport = new KryoSupport(classAndIdList);
    }
    /**
     * Deep copies some object and returns an internal storage-ready copy
     *
     * @param value the value to copy
     * @return the storage-ready copy
     */
    public Element copyForWrite(Element value, ClassLoader loader) {
        if (value == null) {
            return null;

        } else {
            if (value.getObjectValue() == null) {
                return duplicateElementWithNewValue(value, null);
            }

            byte[] bytes = kryoSupport.writeClassAndObject(value.getObjectValue());
            return duplicateElementWithNewValue(value, bytes);
        }
    }

    /**
     * Reconstruct an object from its storage-ready copy.
     *
     * @param storedValue the storage-ready copy
     * @return the original object
     */
    public Element copyForRead(Element storedValue, ClassLoader loader) {
        if (storedValue == null) {
            return null;

        } else {
            if (storedValue.getObjectValue() == null) {
                return duplicateElementWithNewValue(storedValue, null);
            }

            Object object = kryoSupport.readClassAndObject((byte[]) storedValue.getObjectValue());
            return duplicateElementWithNewValue(storedValue, object);
        }
    }

    /**
     * Make a duplicate of an element but using the specified value
     *
     * @param element  the element to duplicate
     * @param newValue the new element's value
     * @return the duplicated element
     */
    public Element duplicateElementWithNewValue(final Element element, final Object newValue) {
        Element newElement;
        if (element.usesCacheDefaultLifespan()) {
            newElement = new Element(element.getObjectKey(), newValue, element.getVersion(),
                    element.getCreationTime(), element.getLastAccessTime(), element.getHitCount(), element.usesCacheDefaultLifespan(),
                    Integer.MIN_VALUE, Integer.MIN_VALUE, element.getLastUpdateTime());
        } else {
            newElement = new Element(element.getObjectKey(), newValue, element.getVersion(),
                    element.getCreationTime(), element.getLastAccessTime(), element.getHitCount(), element.usesCacheDefaultLifespan(),
                    element.getTimeToLive(), element.getTimeToIdle(), element.getLastUpdateTime());
        }
        if (ElementIdHelper.hasId(element)) {
            ElementIdHelper.setId(newElement, ElementIdHelper.getId(element));
        }
        return newElement;
    }

}