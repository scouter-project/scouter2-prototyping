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

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import scouter2.collector.common.kryo.KryoSupportWithTaggedFieldSerializer;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.infrastructure.db.filedb.HourUnitWithMinutes;
import scouter2.collector.infrastructure.db.filedb.MinuteUnitWithSeconds;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-07
 */
public class MapDbObjectSerializer<T> implements Serializer<T>, Serializable {

    KryoSupportWithTaggedFieldSerializer kryoSupport;

    public MapDbObjectSerializer() {
        List<Pair<Class<?>, Integer>> classAndIdList = new ArrayList<>();
        classAndIdList.add(Tuples.pair(TreeSet.class, 21));
        classAndIdList.add(Tuples.pair(HourUnitWithMinutes.class, 22));
        classAndIdList.add(Tuples.pair(Obj.class, 23));
        classAndIdList.add(Tuples.pair(HashMap.class, 24));
        classAndIdList.add(Tuples.pair(MinuteUnitWithSeconds.class, 25));

        kryoSupport = new KryoSupportWithTaggedFieldSerializer(classAndIdList);
    }

    @Override
    public void serialize(@NotNull DataOutput2 out, @NotNull T value) throws IOException {
        byte[] bytes = kryoSupport.writeClassAndObject(value);
        out.packInt(bytes.length);
        out.write(bytes);

    }

    @Override
    public T deserialize(@NotNull DataInput2 input, int available) throws IOException {
        int size = input.unpackInt();
        byte[] bytes = new byte[size];
        input.readFully(bytes);
        return (T) kryoSupport.readClassAndObject(bytes);
    }
}
