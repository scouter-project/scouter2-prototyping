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

package scouter2.collector.infrastructure.repository.passthrough;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.xlog.Xlog;
import scouter2.collector.domain.xlog.XlogOffset;
import scouter2.collector.domain.xlog.XlogRepo;
import scouter2.collector.domain.xlog.XlogRepoAdapter;
import scouter2.collector.domain.xlog.XlogStreamObserver;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.proto.XlogP;

import java.util.function.Consumer;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 09/09/2019
 */
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("none")
public class NoneXlogRepo extends XlogRepoAdapter implements XlogRepo, NonThreadSafeRepo {
    @Override
    public String getRepoType() {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void add(Xlog xlog) {
    }

    @Override
    public void stream(String applicationId, long from, long to, long maxReadCount, Consumer<XlogP> stream) {
    }

    @Override
    public void streamByObjs(String applicationId, LongSet objIds, long from, long to, long maxReadCount,
                             Consumer<XlogP> stream) {
    }

    @Override
    public void streamLatest(String applicationId, XlogOffset lastOffset, int maxReadCount, XlogStreamObserver streamObserver) {
    }

    @Override
    public MutableList<XlogP> findXlogs(MutableSet<byte[]> xlogIds) {
        return null;
    }

    @Override
    public MutableList<XlogP> findXlogsByGxid(byte[] gxid) {
        return null;
    }
}
