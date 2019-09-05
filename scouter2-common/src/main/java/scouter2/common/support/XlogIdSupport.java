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

import org.apache.commons.lang3.StringUtils;
import scouter2.common.util.ByteUtil;
import scouter2.common.util.DateUtil;
import scouter2.common.util.LongUtil;

import java.util.Random;

/**
 * util for Xlog ID generation.
 * xlogId - 256 bits / 16 bytes array.
 *  - [--- most sig (128 bits) ---][--- least sig (128 bits) ---] ==> total 256 bits
 *  - Timed format xlog id - timestamp in minutes included on the most 27 bit.
 *  - untimestamped xlog id - translated as timestamped xlog id on the scouter collector.
 *  - least 2bits in a most sig bits(most 8bytes) indicate the format
 *    - [00] : timestamped xlog id
 *    - [01] : untimestamped xlog id - most sig 128 bits should have 0 except least 2 bits of most sig.
 *    - [10] : full 256bits timestamped xlog id (not yet supported)
 *    - [11] : full 256bits untimestamped xlog id (not yet supported) - most 27 bits should have 0.
 *
 * @author Gun Lee (gunlee01@gmail.com) on 04/09/2019
 */
public class XlogIdSupport {
    public static final char SEPARATOR = '_';
    private static Random random = new Random(System.currentTimeMillis());
    private static long MILLIS_OF_MIN = 1000 * 60;

    public static byte[] createId() {
        long least = random.nextLong();
        return createId(System.currentTimeMillis(), least);
    }

    public static byte[] createId(long timestamp) {
        long stamp = timestamp / 1000 / 60;
        long most = stamp << (64 - 27);
        return ByteUtil.toByteArray(most, random.nextLong());
    }

    public static byte[] createId(long timestamp, long least) {
        long stamp = timestamp / 1000 / 60;
        long most = stamp << (64 - 27);
        return ByteUtil.toByteArray(most, least);
    }

    public static byte[] createId(String yyyymmdd, long least) {
        long timestamp = DateUtil.yyyymmdd(yyyymmdd);
        return createId(timestamp, least);
    }

    public static byte[] createUntimestampedId() {
        long least = random.nextLong();
        return ByteUtil.toByteArray(1L, least);
    }

    public static long timePart(byte[] xlogId) {
        return timePart(xlogId[0], xlogId[1], xlogId[2], xlogId[3]);
    }

    public static long timestamp(byte[] xlogId) {
        return timestamp(xlogId[0], xlogId[1], xlogId[2], xlogId[3]);
    }

    public static long most(byte[] xlogId) {
        return ByteUtil.bytesToLong(xlogId[0], xlogId[1], xlogId[2], xlogId[3],
                xlogId[4], xlogId[5], xlogId[6], xlogId[7]);
    }

    public static long least(byte[] xlogId) {
        return ByteUtil.bytesToLong(xlogId[8], xlogId[9], xlogId[10], xlogId[11],
                xlogId[12], xlogId[13], xlogId[14], xlogId[15]);
    }

    private static long timePart(byte b1, byte b2, byte b3, byte b4) {
        int i = b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
        return i >>> (32 - 27);
    }

    private static long timestamp(byte b1, byte b2, byte b3, byte b4) {
        return timePart(b1, b2, b3, b4) * MILLIS_OF_MIN;
    }

    public static String toString(byte[] xlogId) {
        return LongUtil.toString(most(xlogId)) + SEPARATOR + LongUtil.toString(least(xlogId));
    }

    public static byte[] parseFrom(String sXlogId) {
        String[] splits = StringUtils.split(sXlogId, SEPARATOR);
        return ByteUtil.toByteArray(LongUtil.parseFrom(splits[0]), LongUtil.parseFrom(splits[1]));
    }
}
