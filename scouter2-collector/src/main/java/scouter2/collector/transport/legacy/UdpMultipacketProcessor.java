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

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scouter.util.LongEnumer;
import scouter.util.LongKeyLinkedMap;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigLegacy;
import scouter2.collector.springconfig.ThreadNameDecorator;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-10
 */
@Slf4j
@Component
public class UdpMultipacketProcessor {
    private static final int CAPACITY = 1000;
    public static final ThrottleConfig S_0013 = ThrottleConfig.of("S0013");

    private LongKeyLinkedMap<UdpMultipacket> buffer;
    private ConfigLegacy configLegacy;

    public UdpMultipacketProcessor(ConfigLegacy configLegacy) {
        this.buffer = new LongKeyLinkedMap<UdpMultipacket>().setMax(CAPACITY);
        this.configLegacy = configLegacy;
    }

    @Scheduled(fixedDelay = 1000, initialDelay = 5000)
    public void schedule() {
        ThreadNameDecorator.runWithName(this.getClass().getSimpleName(), () -> {
            if (buffer.size() > 0) {
                try {
                    checkExpired();
                } catch (Exception e) {
                    log.error(e.getMessage(), S_0013, e);
                }
            }
        });
    }

    public byte[] add(long pkid, int total, int num, byte[] data, int objHash, InetAddress addr) throws IOException {
        UdpMultipacket p;
        synchronized (buffer) {
            p = buffer.get(pkid);
            if (p == null) {
                p = new UdpMultipacket(total, objHash, addr);
                buffer.put(pkid, p);
            }
        }

        p.set(num, data);
        if (p.isDone()) {
            buffer.remove(pkid);
            return p.toBytes();
        }
        return null;
    }

    private void checkExpired() {
        LongEnumer en = buffer.keys();
        while (en.hasMoreElements()) {
            long key = en.nextLong();
            UdpMultipacket p = buffer.get(key);
            if (p.isExpired()) {
                buffer.remove(key);
                if (configLegacy.isLegacyLogExpiredMultipacket()) {
                    log.debug("S150 {}", p.toString());
                }
            }
        }
    }
}
