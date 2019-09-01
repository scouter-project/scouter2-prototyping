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
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.common.util.DateUtil;
import scouter2.proto.Metric4RepoP;
import scouter2.proto.ObjP;
import scouter2.proto.TimeTypeP;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

        repo.stream(applicationId, TimeTypeP.REALTIME, testTime - 2000, testTime, consumer(metrics));

        assertThat(metrics.size()).isEqualTo(2);
    }

    @Test
    public void add_and_stream_of_5min_type_data() {
        long testTime = U.now() - dayFlag.incrementAndGet() * DateUtil.MILLIS_PER_DAY;
        Metric4RepoP metric1 = getAny(objId, testTime - 2000, TimeTypeP.FIVE_MIN);
        Metric4RepoP metric2 = getAny(objId, testTime, TimeTypeP.FIVE_MIN);

        repo.add(applicationId, metric1);
        repo.add(applicationId, metric2);

        List<Metric4RepoP> metrics = Lists.mutable.empty();

        repo.stream(applicationId, TimeTypeP.FIVE_MIN, testTime - 2000, testTime, consumer(metrics));

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

        //1st case
        repo.stream(applicationId, TimeTypeP.REALTIME, testTime - 10000, testTime, consumer(metrics));
        assertThat(metrics.size()).isEqualTo(4);

        //2nd case
        metrics = Lists.mutable.empty();
        repo.stream(applicationId, TimeTypeP.REALTIME, testTime - 8000, testTime - 6000, consumer(metrics));
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

        LongSet instanceIds = LongSets.mutable.with(objId);
        repo.streamByObjs(applicationId, instanceIds, TimeTypeP.REALTIME, testTime - 2000, testTime, consumer(metrics));

        assertThat(metrics.size()).isEqualTo(2);
    }

    @NotNull
    private Consumer<Metric4RepoP> consumer(List<Metric4RepoP> metrics) {
        return value -> metrics.add(value);
    }
}