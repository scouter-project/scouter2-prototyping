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

import io.grpc.stub.StreamObserver;
import org.eclipse.collections.api.set.primitive.LongSet;
import org.eclipse.collections.impl.factory.Lists;
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
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.domain.obj.Obj;
import scouter2.common.util.DateUtil;
import scouter2.proto.Metric4RepoP;
import scouter2.proto.ObjP;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static scouter2.fixture.Metric4RepoPFixture.getAny;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-06
 */
public class LocalMetricRepoTest extends LocalRepoTest {

    static AtomicInteger dayFlag = new AtomicInteger();

    @Autowired
    @InjectMocks
    LocalMetricRepo repo;

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
        Metric4RepoP metric1 = getAny(objId, testTime - 2000);
        Metric4RepoP metric2 = getAny(objId, testTime);

        repo.add(applicationId, metric1);
        repo.add(applicationId, metric2);

        List<Metric4RepoP> metrics = Lists.mutable.empty();
        AtomicInteger onCompletedCount = new AtomicInteger();

        repo.streamListByPeriod(applicationId, testTime - 2000, testTime, streamObserver(metrics, onCompletedCount));

        assertThat(onCompletedCount.get()).isEqualTo(1);
        assertThat(metrics.size()).isEqualTo(2);
    }

    @Test
    public void add_and_stream_in_specific_period() {
        long testTime = U.now() - dayFlag.incrementAndGet() * DateUtil.MILLIS_PER_DAY;
        Metric4RepoP metric1 = getAny(objId, testTime - 10000);
        Metric4RepoP metric2 = getAny(objId, testTime - 8000);
        Metric4RepoP metric3 = getAny(objId, testTime - 6000);
        Metric4RepoP metric4 = getAny(objId, testTime - 3000);

        repo.add(applicationId, metric1);
        repo.add(applicationId, metric2);
        repo.add(applicationId, metric3);
        repo.add(applicationId, metric4);

        List<Metric4RepoP> metrics = Lists.mutable.empty();
        AtomicInteger onCompletedCount = new AtomicInteger();

        //1st case
        repo.streamListByPeriod(applicationId, testTime - 10000, testTime, streamObserver(metrics, onCompletedCount));
        assertThat(onCompletedCount.get()).isEqualTo(1);
        assertThat(metrics.size()).isEqualTo(4);

        //2nd case
        metrics = Lists.mutable.empty();
        repo.streamListByPeriod(applicationId, testTime - 8000, testTime - 6000, streamObserver(metrics, onCompletedCount));
        assertThat(metrics.size()).isEqualTo(2);
    }

    @Test
    public void add_and_stream_with_instance_ids() {
        long testTime = U.now() - dayFlag.incrementAndGet() * DateUtil.MILLIS_PER_DAY;
        Metric4RepoP metric1 = getAny(objId, testTime - 2000);
        Metric4RepoP metric2 = getAny(anotherObjId, testTime - 2000);
        Metric4RepoP metric3 = getAny(objId, testTime);
        Metric4RepoP metric4 = getAny(anotherObjId, testTime);

        repo.add(applicationId, metric1);
        repo.add(applicationId, metric2);
        repo.add(applicationId, metric3);
        repo.add(applicationId, metric4);

        List<Metric4RepoP> metrics = Lists.mutable.empty();
        AtomicInteger onCompletedCount = new AtomicInteger();

        LongSet instanceIds = LongSets.mutable.with(objId);
        repo.streamListByPeriodAndObjs(applicationId, instanceIds,testTime - 2000, testTime, streamObserver(metrics, onCompletedCount));

        assertThat(onCompletedCount.get()).isEqualTo(1);
        assertThat(metrics.size()).isEqualTo(2);
    }

    @NotNull
    private StreamObserver<Metric4RepoP> streamObserver(List<Metric4RepoP> metrics, AtomicInteger onCompletedCount) {
        return new StreamObserver<Metric4RepoP>() {
            @Override
            public void onNext(Metric4RepoP value) {
                metrics.add(value);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                fail();
            }

            @Override
            public void onCompleted() {
                onCompletedCount.incrementAndGet();
            }
        };
    }
}