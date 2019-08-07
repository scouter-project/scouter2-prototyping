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
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-05-24
 */
public class ImmutableListSerializer extends Serializer<ImmutableList<Object>> {
	public ImmutableListSerializer() {
		setImmutable(true);
	}

	@Override
	public ImmutableList<Object> read(Kryo kryo, Input input, Class<ImmutableList<Object>> type) {
		final int size = input.readInt(true);
		final Object[] list = new Object[size];
		for (int i = 0; i < size; i++) {
			list[i] = kryo.readClassAndObject(input);
		}
		return Lists.immutable.with(list);

	}

	@Override
	public void write(Kryo kryo, Output output, ImmutableList<Object> list) {
		output.writeInt(list.size(), true);
		for (Object elm : list) {
			kryo.writeClassAndObject(output, elm);
		}
	}
}
