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

package scouter2.collector.infrastructure.repository.local;

import org.eclipse.collections.impl.factory.Lists;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import scouter2.collector.LocalRepoTest;
import scouter2.collector.common.util.U;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.domain.xlog.RealtimeXlogOffset;
import scouter2.collector.domain.xlog.Xlog;
import scouter2.collector.domain.xlog.XlogOffset;
import scouter2.collector.domain.xlog.XlogStreamObserver;
import scouter2.common.util.DateUtil;
import scouter2.fixture.XlogFixture;
import scouter2.proto.ObjP;
import scouter2.proto.XlogP;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 28/08/2019
 */
public class LocalXlogRepoTest4Isolation extends LocalRepoTest {

    static AtomicInteger dayFlag = new AtomicInteger();

    @Autowired
    @InjectMocks
    LocalXlogRepo repo;

    @Mock
    ObjService objService;

    String applicationId = "testapp2";
    long objId = 1;
    long anotherObjId = 2;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        Obj obj = new Obj(objId, ObjP.newBuilder().setApplicationId(applicationId).build());
        when(objService.findById(anyLong())).thenReturn(obj);
    }

    @Test
    public void add_and_stream_to_latest() {
        long testTime = U.now() - dayFlag.incrementAndGet() * DateUtil.MILLIS_PER_DAY;
        Xlog xlog1 = XlogFixture.getOne(testTime - 10000, applicationId, objId);
        Xlog xlog2 = XlogFixture.getOne(testTime - 9000, applicationId, objId);
        Xlog xlog3 = XlogFixture.getOne(testTime - 8000, applicationId, objId);
        Xlog xlog4 = XlogFixture.getOne(testTime - 5000, applicationId, objId);

        repo.add(xlog1);
        repo.add(xlog2);
        repo.add(xlog3);
        repo.add(xlog4);

        List<XlogP> xlogs = Lists.mutable.empty();
        List<RealtimeXlogOffset> offsets = Lists.mutable.empty();

        repo.streamLatest(applicationId, new RealtimeXlogOffset(0, 0), Integer.MAX_VALUE, stream(xlogs, offsets));

        assertThat(xlogs.size()).isEqualTo(4);
        assertThat(offsets.get(0).getIndex()).isGreaterThan(0);
    }

    @Test
    public void add_and_stream_to_latest_with_offset_parameter() {
        long testTime = U.now() - dayFlag.incrementAndGet() * DateUtil.MILLIS_PER_DAY;
        Xlog xlog1 = XlogFixture.getOne(testTime - 10000, applicationId, objId);
        Xlog xlog2 = XlogFixture.getOne(testTime - 9000, applicationId, objId);
        Xlog xlog3 = XlogFixture.getOne(testTime - 5000, applicationId, objId);
        Xlog xlog4 = XlogFixture.getOne(testTime - 5000, applicationId, objId);
        Xlog xlog5 = XlogFixture.getOne(testTime - 5000, applicationId, objId);

        repo.add(xlog1);
        repo.add(xlog2);
        repo.add(xlog3);
        repo.add(xlog4);
        repo.add(xlog5);

        List<XlogP> xlogs = Lists.mutable.empty();
        List<RealtimeXlogOffset> offsets = Lists.mutable.empty();

        repo.streamLatest(applicationId, new RealtimeXlogOffset(0, 0), 3, stream(xlogs, offsets));
        RealtimeXlogOffset lastOffset = offsets.get(0);

        //first try
        assertThat(xlogs.size()).isEqualTo(3);
        assertThat(lastOffset.getIndex()).isGreaterThan(0);
        assertThat(xlogs.get(0).getTxid()).isEqualTo(xlog3.getProto().getTxid());
        assertThat(xlogs.get(2).getTxid()).isEqualTo(xlog5.getProto().getTxid());

        //stream with next offset
        xlogs.clear();
        offsets.clear();

        Xlog xlogAfter1 = XlogFixture.getOne(testTime - 3000, applicationId, objId);
        Xlog xlogAfter2 = XlogFixture.getOne(testTime - 3000, applicationId, objId);
        repo.add(xlogAfter1);
        repo.add(xlogAfter2);

        repo.streamLatest(applicationId, lastOffset, Integer.MAX_VALUE, stream(xlogs, offsets));

        assertThat(xlogs.size()).isEqualTo(2);
        assertThat(xlogs.get(0).getTxid()).isEqualTo(xlogAfter1.getProto().getTxid());
        assertThat(xlogs.get(1).getTxid()).isEqualTo(xlogAfter2.getProto().getTxid());
    }

    @NotNull
    private XlogStreamObserver stream(List<XlogP> xlogs, List<RealtimeXlogOffset> offsets) {
        return new XlogStreamObserver() {
            @Override
            public void onNext(XlogP xlogP) {
                xlogs.add(xlogP);
            }

            @Override
            public void onComplete(XlogOffset offset) {
                offsets.add((RealtimeXlogOffset) offset);
            }
        };
    }

    @NotNull
    private Consumer<XlogP> consumer(List<XlogP> xlogs) {
        return value -> xlogs.add(value);
    }
}