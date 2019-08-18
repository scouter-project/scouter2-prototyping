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
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigLegacy;
import scouter2.collector.main.CoreRun;
import scouter2.collector.transport.legacy.service.LegacyServiceHandlingProxy;
import scouter2.common.util.FileUtil;
import scouter2.common.util.ThreadUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-24
 */
@Slf4j
public class LegacyTcpTransport extends Thread {
    public static final ThrottleConfig S_0030 = ThrottleConfig.of("S0030");
    public static final ThrottleConfig S_0031 = ThrottleConfig.of("S0031");
    private static ExecutorService es;
    private static AtomicInteger threadNo = new AtomicInteger();
    private static LegacyTcpTransport instance;

    private ServerSocket serverSocket;

    private ConfigLegacy conf;
    private LegacyUdpDataProcessor processor;

    public synchronized static LegacyTcpTransport start(ConfigLegacy conf,
                                                        LegacyUdpDataProcessor processor) {
        if (instance != null) {
            throw new RuntimeException("Already working legacy udp receiver exists.");
        }

        //init
        LegacyServiceHandlingProxy.load();

        es = ThreadUtil.createExecutor("LegacyTcpWorker", 2, conf.getLegacyNetTcpServicePoolSize(),
                20000, true);

        instance = new LegacyTcpTransport(conf, processor);
        instance.setDaemon(true);
        instance.setName(ThreadUtil.getName(instance.getClass(), threadNo.getAndIncrement()));
        instance.start();
        return instance;

    }

    private LegacyTcpTransport(ConfigLegacy conf, LegacyUdpDataProcessor processor) {
        this.conf = conf;
        this.processor = processor;
    }

    @Override
    public void run() {
        log.info("\ttcp_port=" + conf.getLegacyNetTcpListenPort());
        log.info("\tcp_agent_so_timeout=" + conf.getLegacyNetTcpAgentSoTimeoutMs());
        log.info("\tcp_client_so_timeout=" + conf.getLegacyNetTcpClientSoTimeoutMs());

        try {
            serverSocket = new ServerSocket(conf.getLegacyNetTcpListenPort(), 50,
                    InetAddress.getByName(conf.getLegacyNetTcpListenIp()));
            open();
        } catch (Exception e) {
            log.error(e.getMessage(), S_0030, e);
        } finally {
            FileUtil.close(serverSocket);
        }
    }

    private void open() throws IOException {
        while (CoreRun.isRunning()) {
            Socket socket = serverSocket.accept();
            socket.setSoTimeout(conf.getLegacyNetTcpClientSoTimeoutMs());
            socket.setReuseAddress(true);
            try {
                es.execute(new LegacyTcpWorker(socket, conf));
            } catch (Exception e) {
                log.error(e.getMessage(), S_0031, e);
            }
        }
    }
}
