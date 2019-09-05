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

package scouter2.common.support;

import org.junit.Test;
import scouter2.common.util.DateUtil;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 04/09/2019
 */
public class XlogIdSupportTest {

    @Test
    public void create_and_check_timestamp() {
        long now = System.currentTimeMillis();
        byte[] id = XlogIdSupport.createId();

        long timestamp = XlogIdSupport.timestamp(id);
        assertThat(timestamp).isBetween(now - 60 * 1000, now);
    }

    @Test
    public void create_and_check_least() {
        long least = new Random().nextLong();
        byte[] id = XlogIdSupport.createId(System.currentTimeMillis(), least);

        assertThat(XlogIdSupport.least(id)).isEqualTo(least);
    }

    @Test
    public void create_and_check_ymd() {
        long least = new Random().nextLong();
        String ymd = "20190301";
        byte[] id = XlogIdSupport.createId(ymd, least);

        long timestampOfId = XlogIdSupport.timestamp(id);
        assertThat(DateUtil.yyyymmdd(timestampOfId)).isEqualTo(ymd);
    }

    @Test
    public void parse_xlogId_test() {
        byte[] id = XlogIdSupport.createId();

        String sXlogId = XlogIdSupport.toString(id);
        byte[] result = XlogIdSupport.parseFrom(sXlogId);

        assertArrayEquals(id, result);
    }
}