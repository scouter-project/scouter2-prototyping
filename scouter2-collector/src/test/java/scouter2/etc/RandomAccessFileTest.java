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

package scouter2.etc;

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
public class RandomAccessFileTest {

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
    public void read_test() throws IOException {

        int int1 = 100;
        int int2 = 200;

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        file.writeInt(int1);
        file.writeInt(int2);

        file.seek(0);
        int readInt1 = file.readInt();
        int readInt2 = file.readInt();

        assertThat(readInt1).isEqualTo(int1);
        assertThat(readInt2).isEqualTo(int2);

        file.seek(0);
        byte[] buffer = new byte[8];
        file.read(buffer);

        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(buffer));
        int streamInt1 = inputStream.readInt();
        int streamInt2 = inputStream.readInt();

        assertThat(streamInt1).isEqualTo(int1);
        assertThat(streamInt2).isEqualTo(int2);
    }

    @Test
    public void beyond_offset_test() throws IOException {
        int int1 = 100;
        int int2 = 200;

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        file.writeInt(int1);
        file.writeInt(int2);

        file.seek(0);
        int readInt1 = file.readInt();
        assertThat(readInt1).isEqualTo(int1);

        byte[] buffer = new byte[8];
        int readSize = file.read(buffer);

        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(buffer));
        int streamInt2 = inputStream.readInt();
        assertThat(streamInt2).isEqualTo(int2);

    }
}
