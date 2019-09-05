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

package scouter2.collector.domain.xlog;

import lombok.Getter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
@Getter
public class XlogUtil {

//    public static long txidDated(long timestamp, XlogP xlogP) {
//        return proto.getTxid();
//    }
//
//    private long genDatedId(long id) {
//        long day = DateUtil.getDayUnit(timestamp);
//        return (day << 49) | (l >>> 15);
//    }

    public static void main(String[] args) {
//        long l = 3051174000566568236L;
//        long dateNo = 7200L;
//
//        long result = (dateNo << 49) | (l >>> 15);
//
//        byte[] resultByte = (dateNo << 49) | (l >>> 15);
//
//        System.out.println(l >>> 15);
//        System.out.println((long) dateNo << 49);
//        System.out.println(result);
//
//        System.out.println(result >>> 49);

        //27

        byte[] randomBytes = new byte[16];
        ThreadLocalRandom.current().nextBytes(randomBytes);

        long stamp = System.currentTimeMillis() / 1000 / 60;
        System.out.println(stamp);
        System.out.println(ZonedDateTime.ofInstant(Instant.ofEpochMilli(stamp * 1000 * 60), ZoneId.systemDefault()));

        long most = ThreadLocalRandom.current().nextLong();
        long least = ThreadLocalRandom.current().nextLong();

        long mostModified = stamp << (64 - 27) | most >>> 27;

        long timePart = mostModified >>> (64 - 27);
        System.out.println(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timePart * 1000 * 60), ZoneId.systemDefault()));

        byte[] bytes = toByteArray(most, least);
        long timePart1 = timePart(bytes[0], bytes[1], bytes[2], bytes[3]);
        System.out.println(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timePart1 * 1000 * 60), ZoneId.systemDefault()));
    }

    public static byte[] toByteArray(long most, long least) {
        byte[] result = new byte[16];
        for (int i = 15; i >= 8; i--) {
            result[i] = (byte) (most & 0xffL);
            most >>= 8;
        }
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (least & 0xffL);
            least >>= 8;
        }
        return result;
    }

    public static long timePart(byte b1, byte b2, byte b3, byte b4) {
        int i = b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
        long timePart = i >>> (32 - 27);
        return timePart;
    }
}
