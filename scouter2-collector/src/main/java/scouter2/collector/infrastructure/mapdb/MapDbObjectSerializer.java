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

package scouter2.collector.infrastructure.mapdb;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import scouter2.collector.common.kryo.KryoSupport;
import scouter2.collector.infrastructure.filedb.HourUnitWithMinutes;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-07
 */
public class MapDbObjectSerializer<T> implements Serializer<T>, Serializable {

    KryoSupport kryoSupport;

    public MapDbObjectSerializer() {
        List<Pair<Class<?>, Integer>> classAndIdList = new ArrayList<>();
        classAndIdList.add(Tuples.pair(TreeSet.class, 21));
        classAndIdList.add(Tuples.pair(HourUnitWithMinutes.class, 22));

        kryoSupport = new KryoSupport(classAndIdList);
    }

    @Override
    public void serialize(@NotNull DataOutput2 out, @NotNull T value) throws IOException {
        out.write(kryoSupport.writeClassAndObject(value));
    }

    @Override
    public T deserialize(@NotNull DataInput2 input, int available) throws IOException {
        byte[] bytes = input.internalByteArray();
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return (T) kryoSupport.readClassAndObject(bytes);
    }
}
