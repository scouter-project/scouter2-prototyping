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

import org.eclipse.collections.api.list.primitive.LongList;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.HTreeMap;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigXlog;
import scouter2.testsupport.T;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 07/09/2019
 */
public class LocalProfileOffsetBufferTest {

    LocalProfileOffsetBuffer sut;

    @Mock
    ConfigXlog configXlog;

    @Mock
    HTreeMap<byte[], LongList> offsetIndex;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        sut = new LocalProfileOffsetBuffer(configXlog);
        when(configXlog.get_xlogProfileOffsetWriteBufferSize()).thenReturn(100);
        when(configXlog.get_xlogProfileOffsetWriteBufferKeepMillis()).thenReturn(500);
    }

    @Test
    public void test_just_add() {
        sut.add(T.xlogId(), 100L, U.now(), offsetIndex);

        assertThat(sut.innerBuffer.size()).isEqualTo(1);
        verify(offsetIndex, times(0)).put(any(), any());
    }

    @Test
    public void persist_after_delaying_time() throws InterruptedException {
        byte[] txid = T.xlogId();
        sut.add(txid, 100L, U.now(), offsetIndex);
        sut.add(txid, 200L, U.now(), offsetIndex);

        assertThat(sut.innerBuffer.size()).isEqualTo(1);
        Thread.sleep(100);
        assertThat(sut.innerBuffer.size()).isEqualTo(1);
        Thread.sleep(500);

        assertThat(sut.innerBuffer.size()).isEqualTo(0);
        verify(offsetIndex, times(1)).put(any(), any());
    }
}