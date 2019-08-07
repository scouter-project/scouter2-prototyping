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

import java.io.IOException;
import java.io.RandomAccessFile;

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

}
