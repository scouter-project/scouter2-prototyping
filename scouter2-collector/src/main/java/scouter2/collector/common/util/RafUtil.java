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

import scouter.io.DataInputX;
import scouter.io.DataOutputX;

import java.io.IOException;
import java.io.RandomAccessFile;

import static scouter.io.DataOutputX.INT3_MAX_VALUE;
import static scouter.io.DataOutputX.INT3_MIN_VALUE;
import static scouter.io.DataOutputX.LONG5_MAX_VALUE;
import static scouter.io.DataOutputX.LONG5_MIN_VALUE;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-06
 */
public class RafUtil {

    /**
     * read given bytes from the RandomAccessFile.
     * if no data to read then return zero size byte array.
     * if read data is smaller than given size then return size-adjusted(for fitting read data) byte array.
     * @param raf
     * @param startOffset
     * @param size
     * @return
     * @throws IOException
     */
    public static byte[] readOfSize(RandomAccessFile raf, long startOffset, int size) throws IOException {
        raf.seek(startOffset);
        byte[] buffer = new byte[size];
        int readSize = raf.read(buffer);

        if (readSize <= 0) {
            return new byte[0];
        }
        if (readSize < size) {
            byte[] resized = new byte[readSize];
            System.arraycopy(buffer, 0, resized, 0, readSize);
            return resized;
        }
        return buffer;
    }

    public void writeDecimal(RandomAccessFile file, long v) throws IOException {
        if (v == 0) {
            file.write(0);
        } else if (Byte.MIN_VALUE <= v && v <= Byte.MAX_VALUE) {
            byte[] b = new byte[2];
            b[0] = 1;
            b[1] = (byte) v;
            file.write(b);
        } else if (Short.MIN_VALUE <= v && v <= Short.MAX_VALUE) {
            byte[] b = new byte[3];
            b[0] = 2;
            DataOutputX.toBytes(b, 1, (short) v);
            file.write(b);
        } else if (INT3_MIN_VALUE <= v && v <= INT3_MAX_VALUE) {
            byte[] b = new byte[4];
            b[0] = 3;
            file.write(DataOutputX.toBytes3(b, 1, (int) v), 0, 4);
        } else if (Integer.MIN_VALUE <= v && v <= Integer.MAX_VALUE) {
            byte[] b = new byte[5];
            b[0] = 4;
            file.write(DataOutputX.toBytes(b, 1, (int) v), 0, 5);
        } else if (LONG5_MIN_VALUE <= v && v <= LONG5_MAX_VALUE) {
            byte[] b = new byte[6];
            b[0] = 5;
            file.write(DataOutputX.toBytes5(b, 1, v), 0, 6);
        } else if (Long.MIN_VALUE <= v && v <= Long.MAX_VALUE) {
            byte[] b = new byte[9];
            b[0] = 8;
            file.write(DataOutputX.toBytes(b, 1, v), 0, 9);
        }
    }

    public static long readDecimal(RandomAccessFile file) throws IOException {
        byte len = file.readByte();
        switch (len) {
            case 0:
                return 0;
            case 1:
                return file.readByte();
            case 2:
                return file.readShort();
            case 3:
                return DataInputX.toInt3(read(file, 3), 0);
            case 4:
                return file.readInt();
            case 5:
                return DataInputX.toLong5(read(file, 5), 0);
            default:
                return file.readLong();
        }
    }

    public static byte[] read(RandomAccessFile file, int len) throws IOException {
        byte[] buff = new byte[len];
        file.readFully(buff);
        return buff;
    }

    private static int readInt3(RandomAccessFile file) throws IOException {
        int ch1 = file.readByte() & 0xff;
        int ch2 = file.readByte() & 0xff;
        int ch3 = file.readByte() & 0xff;

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8)) >> 8;
    }

    private static int readLong5(RandomAccessFile file) throws IOException {
        int ch1 = file.readByte() & 0xff;
        int ch2 = file.readByte() & 0xff;
        int ch3 = file.readByte() & 0xff;

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8)) >> 8;
    }
}
