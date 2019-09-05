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
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.esotericsoftware.kryo.serializers.EnumNameSerializer;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.api.tuple.Pair;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Getter
@Setter
public class KryoSupportWithTaggedFieldSerializer {
	private KryoPool kryoPool;

	public KryoSupportWithTaggedFieldSerializer(List<Pair<Class<?>, Integer>> classAndIdList) {
		KryoFactory factory = () -> {
			Kryo kryo = new Kryo();
			kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
			kryo.setDefaultSerializer(TaggedFieldSerializer.class);
			kryo.getTaggedFieldSerializerConfig().setSkipUnknownTags(true);
			kryo.addDefaultSerializer(Enum.class, EnumNameSerializer.class);
			kryo.addDefaultSerializer(ImmutableList.class, ImmutableListSerializer.class);
			kryo.addDefaultSerializer(MutableList.class, MutableListSerializer.class);
			kryo.addDefaultSerializer(MutableLongList.class, MutableLongListSerializer.class);
			for (Pair<Class<?>, Integer> classAndId : classAndIdList) {
				kryo.register(classAndId.getOne(), classAndId.getTwo());
			}
			return kryo;
		};
		kryoPool = new KryoPool.Builder(factory).softReferences().build();
	}

	public <T> T readObject(byte[] bytes, final Class<T> classType) {
		return read((kryo, input) -> kryo.readObject(input, classType), bytes);
	}

	public <T> T readObjectOrNull(byte[] bytes, final Class<T> classType) {
		return read((kryo, input) -> kryo.readObjectOrNull(input, classType), bytes);
	}

	public Object readClassAndObject(byte[] bytes) {
		return read(Kryo::readClassAndObject, bytes);
	}

	public byte[] writeObject(final Object object) {
		return write((kryo, output) -> kryo.writeObject(output, object), object);
	}

	public byte[] writeObjectOrNull(final Object object) {
		return write((kryo, output) -> kryo.writeObjectOrNull(output, object, object.getClass()), object);
	}

	public byte[] writeClassAndObject(final Object object) {
		return write((kryo, output) -> kryo.writeClassAndObject(output, object), object);
	}

	private byte[] write(BiConsumer<Kryo, Output> callback, Object object) {
		Kryo kryo = null;
		byte[] bytes;
		Output output = null;
		try {
			kryo = kryoPool.borrow();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			output = new Output(outputStream);

			callback.accept(kryo, output);

			output.flush();
			bytes = outputStream.toByteArray();

		} finally {
			if (output != null) {
				output.close();
			}

			if (kryo != null)
				kryoPool.release(kryo);
		}
		return bytes;
	}

	private <T> T read(BiFunction<Kryo, Input, T> callback, byte[] bytes) {
		Kryo kryo = null;
		T result;
		Input input = null;
		try {
			kryo = kryoPool.borrow();
			input = new Input(bytes);

			result = callback.apply(kryo, input);

		} finally {
			if (input != null) {
				input.close();
			}

			if (kryo != null)
				kryoPool.release(kryo);
		}
		return result;
	}
}
