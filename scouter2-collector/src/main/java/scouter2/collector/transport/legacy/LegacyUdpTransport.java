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
import scouter2.collector.config.ConfigLegacy;
import scouter2.collector.domain.xlog.XlogReceiveQueue;
import scouter2.common.util.FileUtil;
import scouter2.common.util.ThreadUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-24
 */
@Slf4j
public class LegacyUdpTransport extends Thread {
    private static AtomicInteger threadNo = new AtomicInteger();
    private static LegacyUdpTransport instance;
    DatagramSocket udpsocket;

    private ConfigLegacy conf;
    private XlogReceiveQueue xlogReceiveQueue;
    private LegacyUdpDataProcessor processor;

    public synchronized static LegacyUdpTransport start(ConfigLegacy conf,
                                                        LegacyUdpDataProcessor processor) {
        if (instance != null) {
            throw new RuntimeException("Already working legacy udp receiver exists.");
        }
        instance = new LegacyUdpTransport(conf, processor);
        instance.setDaemon(true);
        instance.setName(ThreadUtil.getName(instance.getClass(), threadNo.getAndIncrement()));
        instance.start();
        return instance;
    }

    private LegacyUdpTransport(ConfigLegacy conf, LegacyUdpDataProcessor processor) {
        this.conf = conf;
        this.processor = processor;
    }

    @Override
    public void run() {
        try {
            while (true) {
                open(conf.getNetUdpListenIp(), conf.getNetUdpListenPort());
                recv();
                FileUtil.close(udpsocket);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void open(String host, Integer port) {
        log.info("legacy udp listen " + host + ":" + port);
        log.info("legacy udp_buffer " + conf.getNetUdpPacketBufferSize());
        log.info("legacy udp_so_rcvbuf " + conf.getNetUdpSoRcvbufSize());

        while (true) {
            try {
                udpsocket = new DatagramSocket(port, InetAddress.getByName(host));
                int buf = conf.getNetUdpSoRcvbufSize();
                if (buf > 0) {
                    udpsocket.setReceiveBufferSize(buf);
                }
                return ;
            } catch (Exception e) {
                log.info("S157", 1, "udp data server port=" + port, e);
            }
            ThreadUtil.sleep(3000);
        }
    }

    private void recv() {
        try {
            int bufferSize = conf.getNetUdpPacketBufferSize();
            byte[] rbuf = new byte[bufferSize];
            DatagramPacket packet = new DatagramPacket(rbuf, bufferSize);

            // loop until any exception
            while (true) {
                udpsocket.receive(packet);
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
                processor.offer(data, packet.getAddress());
            }
        } catch(Throwable t) {
            log.error("S151", 10, t);
        }
    }
}
