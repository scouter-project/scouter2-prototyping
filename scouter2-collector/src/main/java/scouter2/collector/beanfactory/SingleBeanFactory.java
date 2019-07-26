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
package scouter2.collector.beanfactory;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public class SingleBeanFactory {
    private static MutableMap<Class, Object> beans = Maps.mutable.empty();

    public static void addBean(Class<?> clazz, Object object) {
        if (!object.getClass().isAssignableFrom(clazz)) {
            throw new ClassCastException("Can not apply as " + clazz.getName()
                    + " from object " + object.getClass().getName());
        }
        beans.put(clazz, object);
    }

    public static <T> T getBean(Class<T> clazz) {
        Object o = beans.get(clazz);
        if (o == null) {
            throw new NoSuchBeanException("Can not load bean " + clazz.getName());
        }
        return (T) o;
    }
}
