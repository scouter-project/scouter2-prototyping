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
import org.apache.commons.lang3.StringUtils;
import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.net.NetCafe;
import scouter.net.RequestCmd;
import scouter.net.TcpFlag;
import scouter.util.FileUtil;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigLegacy;
import scouter2.collector.main.CoreRun;
import scouter2.collector.transport.legacy.agent.LegacyTcpAgentManager;
import scouter2.collector.transport.legacy.agent.LegacyTcpAgentWorker;
import scouter2.collector.transport.legacy.service.LegacyServiceHandlingProxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import static scouter2.collector.transport.legacy.LegacyTcpServiceWorker.WorkerContainer.container;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-11
 */
@Slf4j
public class LegacyTcpServiceWorker implements Runnable {

    public static final ThrottleConfig S_0029 = ThrottleConfig.of("S0029");
    public static final ThrottleConfig S_0035 = ThrottleConfig.of("S0035");

    static class WorkerContainer {
        static WorkerContainer container = new WorkerContainer();
        ThreadLocal<String> remoteIpByWorker = new ThreadLocal<>();
        int workers = 0;
        void inc() {
            synchronized (this) {
                workers += 1;
            }
        }
        void dec() {
            synchronized (this) {
                workers -= 1;
            }
        }
        int getActiveCount() {
            return workers;
        }
    }

    Socket socket;
    ConfigLegacy conf;
    LegacyTcpAgentManager agentManager;
    DataInputX in;
    DataOutputX out;

    public LegacyTcpServiceWorker(Socket socket, ConfigLegacy conf, LegacyTcpAgentManager agentManager) throws IOException {
        this.socket = socket;
        this.conf = conf;
        this.agentManager = agentManager;
        in = new DataInputX(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputX(new BufferedOutputStream(socket.getOutputStream()));
    }

    @Override
    public void run() {
        String remoteAddr;
        try {
            remoteAddr = "" + socket.getRemoteSocketAddress();
            String ip = StringUtils.substringAfterLast(socket.getInetAddress().toString(), "/");
            if (StringUtils.isBlank(ip)) {
                ip = socket.getInetAddress().toString();
            }
            container.remoteIpByWorker.set(ip);

            int cafe = in.readInt();
            switch (cafe) {
                case NetCafe.TCP_AGENT:
                case NetCafe.TCP_AGENT_V2:
                    int objHash = in.readInt();
                    int agentCount = agentManager.add(objHash, new LegacyTcpAgentWorker(conf, socket, in, out, cafe));

                    if (conf.isLegacyLogTcpActionEnabled()) {
                        log.info("agent : open. {}[{}] #{}", remoteAddr, objHash, agentCount);
                    }
                    return;

                case NetCafe.TCP_CLIENT:
                    if (conf.isLegacyLogTcpActionEnabled()) {
                        container.inc();
                        log.info("Legacy client : {} open, count:{}", remoteAddr, (container.getActiveCount()));
                    }
//                    if (conf.log_tcp_action_enabled) {
//                        Logger.println("Client : " + remoteAddr + " open #" + (ServiceWorker.getActiveCount() + 1));
//                    }
                    break;
                default:
//                    if (conf.log_tcp_action_enabled) {
//                        Logger.println("Unknown : " + remoteAddr + " drop");
//                    }
//                    FileUtil.close(in);
//                    FileUtil.close(out);
//                    FileUtil.close(socket);
                    return;
            }

        } catch (Throwable t) {
            log.error(t.getMessage(), S_0029, t);
            container.dec();
            if (conf.isLegacyLogTcpActionEnabled()) {
                log.info("Legacy client : closed by open error, count:{}", (container.getActiveCount() + 1));
            }

            FileUtil.close(in);
            FileUtil.close(out);
            FileUtil.close(socket);
            return;
        }

        try {
            boolean sessionOk = false;
            while (CoreRun.isRunning()) {
                String cmd = in.readText();
                if (RequestCmd.CLOSE.equals(cmd)) {
                    log.info("Legacy client sent CLOSE sign : {}", remoteAddr);
                    return;
                }
                long session = in.readLong();
                if (!sessionOk && !RequestCmd.isFreeCmd(cmd)) {
                    //TODO LoginManager
//                    sessionOk = LoginManager.okSession(session);
//                    if (sessionOk == false) {
//                        out.writeByte(TcpFlag.INVALID_SESSION);
//                        out.flush();
//                        throw new RuntimeException("Invalid session key : " + cmd);
//                    }
                }
//                RequestLogger.getInstance().add(cmd, session);
                if (conf.isLegacyLogClientCallCmd()) {
                    log.debug("LegacyCmd Call: {}", cmd);
                }
                LegacyServiceHandlingProxy.process(cmd, in, out, sessionOk);

                out.writeByte(TcpFlag.NoNEXT);
                out.flush();
            }
        } catch (Throwable t) {
            if (t instanceof java.io.EOFException) {
                log.debug("EOF of TCP client. {}" + remoteAddr);

            } else {
                log.error(t.getMessage(), S_0035, t);
                if (conf.isLegacyLogTcpActionEnabled()) {
                    log.info("Legacy client : {} closed, count:{}", remoteAddr, container.getActiveCount(), t);
                }
            }
        } finally {
            FileUtil.close(in);
            FileUtil.close(out);
            FileUtil.close(socket);
            container.dec();
        }
    }
}
