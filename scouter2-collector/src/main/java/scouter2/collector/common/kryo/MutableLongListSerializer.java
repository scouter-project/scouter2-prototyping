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

package scouter2.collector.common.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.impl.factory.primitive.LongLists;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-05-24
 */
public class MutableLongListSerializer extends Serializer<MutableLongList> {
	public MutableLongListSerializer() {
		setImmutable(true);
	}

	@Override
	public MutableLongList read(Kryo kryo, Input input, Class<MutableLongList> type) {
		final int size = input.readVarInt(true);
		final MutableLongList list = LongLists.mutable.empty();

		for (int i = 0; i < size; i++) {
			list.add(input.readVarLong(false));
		}
		return list;
	}

	@Override
	public void write(Kryo kryo, Output output, MutableLongList list) {
		output.writeVarInt(list.size(), true);
		for (int i = 0; i < list.size(); i++) {
			output.writeVarLong(list.get(i), false);
		}
	}
}
