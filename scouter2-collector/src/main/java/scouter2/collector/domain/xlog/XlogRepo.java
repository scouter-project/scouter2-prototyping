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
import scouter2.collector.domain.ScouterRepo;
import scouter2.proto.XlogP;

import java.util.function.Consumer;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public interface XlogRepo extends ScouterRepo {
    void add(String applicationId, Xlog xlog);

    void stream(String applicationId, long from, long to, Consumer<XlogP> stream);

    void streamByObjs(String applicationId, LongSet objIds, long from, long to, Consumer<XlogP> stream);

    void streamLatest(String applicationId, @Nullable XlogOffset lastOffset, int maxCount, XlogStreamObserver stream);

    MutableList<XlogP> findXlogs(String applicationId, MutableSet<byte[]> xlogIds);

    MutableList<XlogP> findXlogsByGxid(String applicationId, byte[] gxid);
}
