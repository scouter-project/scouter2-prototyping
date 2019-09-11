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

package scouter2.collector.domain.xlog;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.springframework.lang.Nullable;
import scouter2.proto.XlogP;

import java.util.function.Consumer;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 28/08/2019
 */
public abstract class XlogRepoAdapter implements XlogRepo {
    @Override
    public void add(Xlog xlog) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stream(String applicationId, long from, long to, long maxReadCount, Consumer<XlogP> stream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void streamByObjs(String applicationId, LongSet objIds, long from, long to, long maxReadCount,
                             Consumer<XlogP> consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void streamLatest(String applicationId, @Nullable XlogOffset lastOffset, int maxReadCount,
                             XlogStreamObserver streamObserver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutableList<XlogP> findXlogs(MutableSet<byte[]> xlogIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutableList<XlogP> findXlogsByGxid(byte[] gxid) {
        throw new UnsupportedOperationException();
    }
}
