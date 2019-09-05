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

package scouter2.collector.infrastructure.db.mapdb;

import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.eclipse.collections.impl.list.mutable.primitive.LongArrayList;
import org.eclipse.collections.impl.tuple.Tuples;
import org.junit.Test;
import scouter2.collector.common.kryo.KryoSupportWithTaggedFieldSerializer;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.infrastructure.db.filedb.HourUnitWithMinutes;
import scouter2.collector.infrastructure.db.filedb.MinuteUnitWithSeconds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 03/09/2019
 */
public class MapDbObjectSerializerTest {

    @Test
    public void test() {
        List<Pair<Class<?>, Integer>> classAndIdList = new ArrayList<>();
        classAndIdList.add(Tuples.pair(TreeSet.class, 21));
        classAndIdList.add(Tuples.pair(HourUnitWithMinutes.class, 22));
        classAndIdList.add(Tuples.pair(Obj.class, 23));
        classAndIdList.add(Tuples.pair(HashMap.class, 24));
        classAndIdList.add(Tuples.pair(MinuteUnitWithSeconds.class, 25));
        classAndIdList.add(Tuples.pair(LongArrayList.class, 27));

        KryoSupportWithTaggedFieldSerializer kryoSupport = new KryoSupportWithTaggedFieldSerializer(classAndIdList);

        byte[] bytes = kryoSupport.writeClassAndObject(LongLists.mutable.of(1000L, 2000L));
        MutableLongList des = (MutableLongList) kryoSupport.readClassAndObject(bytes);

        System.out.println(bytes);
        System.out.println(des);
    }

}