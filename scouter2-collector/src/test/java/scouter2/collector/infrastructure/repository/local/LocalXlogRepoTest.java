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

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.collections.impl.factory.primitive.LongSets;
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
import scouter2.common.util.ThreadUtil;
import scouter2.fixture.XlogFixture;
import scouter2.proto.ObjP;
import scouter2.proto.XlogP;
import scouter2.testsupport.T;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 28/08/2019
 */
public class LocalXlogRepoTest extends LocalRepoTest {

    static AtomicInteger dayFlag = new AtomicInteger();

    @Autowired
    @InjectMocks
    LocalXlogRepo repo;

    @Mock
    ObjService objService;

    String applicationId = "testapp";
    long objId = 1;
    long anotherObjId = 2;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        Obj obj = new Obj(objId, ObjP.newBuilder().setApplicationId(applicationId).build());
        when(objService.findById(anyLong())).thenReturn(obj);
    }

    @Test
    public void add_and_stream() {
        long testTime = U.now() - dayFlag.incrementAndGet() * DateUtil.MILLIS_PER_DAY;
        Xlog xlog1 = XlogFixture.getOne(testTime - 2000, applicationId, objId);
        Xlog xlog2 = XlogFixture.getOne(testTime, applicationId, objId);

        repo.add(xlog1);
        repo.add(xlog2);

        List<XlogP> xlogs = Lists.mutable.empty();

        repo.streamByObjs(applicationId, LongSets.mutable.of(objId), testTime - 2000, testTime,  Integer.MAX_VALUE,
                consumer(xlogs));

        assertThat(xlogs.size()).isEqualTo(2);
    }

    @Test
    public void add_and_stream_in_specific_period() {
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

        //1st case
        repo.streamByObjs(applicationId, LongSets.mutable.of(objId), testTime - 9000, testTime - 7000,
                Integer.MAX_VALUE, consumer(xlogs));
        assertThat(xlogs.size()).isEqualTo(2);

        //2nd case
        xlogs = Lists.mutable.empty();
        repo.streamByObjs(applicationId, LongSets.mutable.of(objId), testTime - 9000, testTime, Integer.MAX_VALUE,
                consumer(xlogs));
        assertThat(xlogs.size()).isEqualTo(3);
    }

    @Test
    public void add_and_get_xlogs() {
        long testTime = U.toMillis(LocalDateTime.of(2017, 8, 9, 15, 30));
        Xlog xlog1 = XlogFixture.getOne(testTime - 10000, applicationId, objId);
        Xlog xlog2 = XlogFixture.getOne(testTime - 9000, applicationId, objId);
        Xlog xlog3 = XlogFixture.getOne(testTime - 8000, applicationId, objId);
        Xlog xlog4 = XlogFixture.getOne(testTime - 5000, applicationId, objId);

        repo.add(xlog1);
        repo.add(xlog2);
        repo.add(xlog3);
        repo.add(xlog4);

        MutableList<XlogP> xlogs = repo.findXlogs(
                Sets.mutable.of(xlog1, xlog2, xlog3, xlog4).collect(x -> x.getProto().getTxid().toByteArray()));

        assertThat(xlogs.size()).isEqualTo(4);
    }

    @Test
    public void add_and_get_some_xlogs() {
        long testTime = U.toMillis(LocalDateTime.of(2017, 8, 10, 15, 30));
        Xlog xlog1 = XlogFixture.getOne(testTime - 10000, applicationId, objId);
        Xlog xlog2 = XlogFixture.getOne(testTime - 9000, applicationId, objId);
        Xlog xlog3 = XlogFixture.getOne(testTime - 8000, applicationId, objId);
        Xlog xlog4 = XlogFixture.getOne(testTime - 5000, applicationId, objId);

        repo.add(xlog1);
        repo.add(xlog2);
        repo.add(xlog3);
        repo.add(xlog4);

        MutableSet<byte[]> xlogIds = Sets.mutable
                .of(xlog1, xlog3)
                .collect(x -> x.getProto().getTxid().toByteArray());
        MutableList<XlogP> xlogs = repo.findXlogs(xlogIds);

        assertThat(xlogs.size()).isEqualTo(2);
        assertThat(xlogs.collect(XlogP::getTxid)).containsOnly(xlog1.getProto().getTxid(), xlog3.getProto().getTxid());
    }

    @Test
    public void add_and_get_xlogs_by_gxid() {
        long testTime = U.toMillis(LocalDateTime.of(2017, 8, 11, 15, 30));

        byte[] gxid = T.xlogId(testTime);
        Xlog xlog1 = XlogFixture.getOneOfGxid(gxid, testTime - 10000, applicationId, objId);
        Xlog xlog2 = XlogFixture.getOne(testTime - 9000, applicationId, objId);
        Xlog xlog3 = XlogFixture.getOneOfGxid(gxid, testTime - 8000, applicationId, objId);
        Xlog xlog4 = XlogFixture.getOne(testTime - 5000, applicationId, objId);

        repo.add(xlog1);
        repo.add(xlog2);
        repo.add(xlog3);
        repo.add(xlog4);

        MutableList<XlogP> xlogs = repo.findXlogsByGxid(gxid);

        assertThat(xlogs.size()).isEqualTo(2);
        assertThat(xlogs.collect(XlogP::getTxid)).containsOnly(xlog1.getProto().getTxid(), xlog3.getProto().getTxid());
    }

    @Test
    public void add_and_get_xlogs_by_gxid_after_gxid_buffer_flush() {
        long testTime = U.toMillis(LocalDateTime.of(2017, 8, 12, 15, 30));

        byte[] gxid = T.xlogId(testTime);
        Xlog xlog1 = XlogFixture.getOneOfGxid(gxid, testTime - 10000, applicationId, objId);
        Xlog xlog2 = XlogFixture.getOne(testTime - 9000, applicationId, objId);
        Xlog xlog3 = XlogFixture.getOneOfGxid(gxid, testTime - 8000, applicationId, objId);
        Xlog xlog4 = XlogFixture.getOne(testTime - 5000, applicationId, objId);
        Xlog xlog5 = XlogFixture.getOneOfGxid(gxid, gxid, testTime - 8000, applicationId, objId);

        repo.add(xlog1);
        repo.add(xlog2);
        repo.add(xlog3);
        repo.add(xlog4);
        repo.add(xlog5);

        ThreadUtil.sleep(500);
        MutableList<XlogP> xlogs = repo.findXlogsByGxid(gxid);

        assertThat(xlogs.size()).isEqualTo(3);
        assertThat(xlogs.collect(XlogP::getTxid)).containsOnly(
                xlog1.getProto().getTxid(),
                xlog3.getProto().getTxid(),
                xlog5.getProto().getTxid()
        );
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