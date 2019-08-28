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

import io.grpc.stub.StreamObserver;
import org.eclipse.collections.api.set.primitive.LongSet;
import scouter2.collector.domain.ScouterRepo;
import scouter2.proto.XlogP;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public interface XlogRepo extends ScouterRepo {
    void add(String applicationId, Xlog xlog);

    void stream(String applicationId, long from, long to, StreamObserver<XlogP> stream);

    void streamByObjs(String applicationId, LongSet objIds, long from, long to, StreamObserver<XlogP> stream);
}
