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

package scouter2.collector.common.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-06
 */
public class RafUtilTest {

    String tempDir = System.getProperty("java.io.tmpdir");
    String fileName = tempDir + "/test-random-access";

    @Before
    public void init() {
        File file = new File(fileName);
        if (file.canRead()) {
            file.delete();
        }
    }

    @After
    public void destroy() {
        File file = new File(fileName);
        if (file.canRead()) {
            file.delete();
        }
    }

    @Test
    public void read_data_test() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        raf.writeInt(100);
        raf.writeInt(200);

        byte[] bytes = RafUtil.readOfSize(raf, 0, 8);

        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(bytes));
        int streamInt1 = inputStream.readInt();
        int streamInt2 = inputStream.readInt();

        assertThat(streamInt1).isEqualTo(100);
        assertThat(streamInt2).isEqualTo(200);
    }

    @Test
    public void read_size_is_equals_to_data_size() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        raf.writeInt(100);
        raf.writeInt(200);

        assertThat(RafUtil.readOfSize(raf, 0, 8).length).isEqualTo(8);
        assertThat(RafUtil.readOfSize(raf, 0, 16).length).isEqualTo(8);
        assertThat(RafUtil.readOfSize(raf, 4, 8).length).isEqualTo(4);
        assertThat(RafUtil.readOfSize(raf, 16, 8).length).isEqualTo(0);
    }
}