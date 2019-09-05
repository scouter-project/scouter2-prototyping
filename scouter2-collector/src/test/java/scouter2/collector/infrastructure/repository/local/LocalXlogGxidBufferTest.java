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
import org.junit.Before;
import org.junit.Test;
import org.mapdb.HTreeMap;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigXlog;
import scouter2.testsupport.T;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 03/09/2019
 */
public class LocalXlogGxidBufferTest {

    LocalXlogGxidBuffer sut;

    @Mock
    ConfigXlog configXlog;

    @Mock
    HTreeMap<byte[], MutableList<byte[]>> gxidIndex;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        sut = new LocalXlogGxidBuffer(configXlog);
        when(configXlog.get_xlogGxidWriteBufferSize()).thenReturn(100);
        when(configXlog.get_xlogGxidWriteBufferKeepMillis()).thenReturn(500);
    }

    @Test
    public void test_just_add() {
        sut.add(T.xlogId(), T.xlogId(), U.now(), gxidIndex);

        assertThat(sut.innerBuffer.size()).isEqualTo(1);
        verify(gxidIndex, times(0)).put(any(), any());
    }

    @Test
    public void persist_after_delaying_time() throws InterruptedException {
        byte[] gxid = T.xlogId();
        sut.add(gxid, T.xlogId(), U.now(), gxidIndex);
        sut.add(gxid, gxid, U.now(), gxidIndex);

        assertThat(sut.innerBuffer.size()).isEqualTo(1);
        Thread.sleep(100);
        assertThat(sut.innerBuffer.size()).isEqualTo(1);
        Thread.sleep(500);

        assertThat(sut.innerBuffer.size()).isEqualTo(0);
        verify(gxidIndex, times(1)).put(any(), any());
    }

    @Test
    public void persist_after_delaying_time_but_remain_no_entry_pointed_gxid() throws InterruptedException {
        byte[] firstGxid = T.xlogId();
        byte[] secondGxid = T.xlogId();

        sut.add(firstGxid, T.xlogId(), U.now(), gxidIndex);
        sut.add(firstGxid, T.xlogId(), U.now(), gxidIndex);
        sut.add(secondGxid, T.xlogId(), U.now(), gxidIndex);
        sut.add(secondGxid, secondGxid, U.now(), gxidIndex);

        assertThat(sut.innerBuffer.size()).isEqualTo(2);
        Thread.sleep(100);
        assertThat(sut.innerBuffer.size()).isEqualTo(2);
        Thread.sleep(500);

        assertThat(sut.innerBuffer.size()).isEqualTo(1);
        assertThat(sut.innerBuffer.getFirst().getGxid()).isEqualTo(firstGxid);
        verify(gxidIndex, times(1)).put(any(), any());
    }
}