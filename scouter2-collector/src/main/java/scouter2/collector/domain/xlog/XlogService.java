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

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.impl.factory.Sets;
import org.springframework.stereotype.Component;
import scouter2.proto.XlogP;

import java.util.function.Consumer;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-09-08
 */
@Component
@Slf4j
public class XlogService {
    private static XlogService instance;

    private XlogRepo repo;

    public XlogService(XlogRepo repo) {
        synchronized (XlogService.class) {
            if (instance != null) {
                throw new IllegalStateException();
            }
            this.repo = repo;
            instance = this;
        }
    }

    public static XlogService getInstance() {
        return instance;
    }

    public XlogP find(byte[] txid) {
        return repo.findXlogs(Sets.mutable.of(txid)).getFirstOptional().orElse(null);
    }

    public MutableList<XlogP> findList(MutableSet<byte[]> txids) {
        return repo.findXlogs(txids);
    }

    public MutableList<XlogP> findListByGxid(byte[] gxid) {
        return repo.findXlogsByGxid(gxid);
    }

    public void streamByObjs(String applicationId, LongSet objIds, long from, long to, long maxReadCount, Consumer<XlogP> consumer) {
        repo.streamByObjs(applicationId, objIds, from, to, maxReadCount, consumer);
    }
}
