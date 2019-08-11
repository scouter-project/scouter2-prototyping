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

package scouter2.collector.transport.legacy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-10
 */
public class UdpMultipacket {
    private int total;
    private int objHash;
    private InetAddress addr;

    private int added = 0;
    private byte[][] data = null;
    private long openTime = System.currentTimeMillis();

    public UdpMultipacket(int total, int objHash, InetAddress addr) {
        this.total = total;
        this.objHash = objHash;
        this.addr = addr;
    }

    public void set(int n, byte[] data) {
        if (n < total) {
            if (this.data[n] == null)
                added += 1;
            this.data[n] = data;
        }
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - this.openTime) >= 1000;
    }

    public boolean isDone() {
        return total == added;
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < total; i++) {
            out.write(this.data[i]);
        }
        return out.toByteArray();
    }

    @Override
    public String toString() {
        return "UdpMultipacket{" +
                "total=" + total +
                ", objHash=" + objHash +
                ", addr=" + addr +
                ", added=" + added +
                '}';
    }
}
