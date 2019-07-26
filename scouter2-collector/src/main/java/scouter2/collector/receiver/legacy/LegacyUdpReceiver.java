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

package scouter2.collector.receiver.legacy;

import lombok.extern.slf4j.Slf4j;
import scouter2.collector.config.ConfigLegacy;
import scouter2.collector.domain.xlog.XlogAdder;
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
public class LegacyUdpReceiver extends Thread {
    private static AtomicInteger threadNo = new AtomicInteger();
    private static LegacyUdpReceiver instance;
    DatagramSocket udpsocket;

    private ConfigLegacy conf;
    private XlogReceiveQueue xlogReceiveQueue;
    private XlogAdder xlogAdder;

    public synchronized static LegacyUdpReceiver start(ConfigLegacy conf,
                                                       XlogAdder xlogAdder) {
        if (instance != null) {
            throw new RuntimeException("Already working legacy udp receiver exists.");
        }
        instance = new LegacyUdpReceiver(conf, xlogAdder);
        instance.setDaemon(true);
        instance.setName(ThreadUtil.getName(instance.getClass(), threadNo.getAndIncrement()));
        instance.start();
        return instance;
    }

    private LegacyUdpReceiver(ConfigLegacy conf, XlogAdder xlogAdder) {
        this.conf = conf;
        this.xlogAdder = xlogAdder;
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
            DatagramPacket p = new DatagramPacket(rbuf, bufferSize);

            // loop until any exception
            while (true) {
                udpsocket.receive(p);
                byte[] data = new byte[p.getLength()];
                System.arraycopy(p.getData(), 0, data, 0, p.getLength());
                //TODO NetDataProcessor.add(data, p.getAddress());
            }
        } catch(Throwable t) {
            log.error("S151", 10, t);
        }
    }
}
