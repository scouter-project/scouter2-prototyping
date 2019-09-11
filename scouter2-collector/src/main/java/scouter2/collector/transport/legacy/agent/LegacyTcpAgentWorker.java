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

package scouter2.collector.transport.legacy.agent;

import lombok.extern.slf4j.Slf4j;
import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.lang.pack.MapPack;
import scouter.lang.pack.Pack;
import scouter.net.NetCafe;
import scouter.net.TcpFlag;
import scouter.util.FileUtil;
import scouter2.collector.common.functional.TriProcedure;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigLegacy;

import java.net.Socket;
import java.net.SocketAddress;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-11
 */
@Slf4j
public class LegacyTcpAgentWorker {

    static final ThrottleConfig S_0067 = ThrottleConfig.of("S0067");
    static final ThrottleConfig S_0068 = ThrottleConfig.of("S0068");
    static final ThrottleConfig S_0069 = ThrottleConfig.of("S0069");
    static final ThrottleConfig S_0070 = ThrottleConfig.of("S0070");
    static final ThrottleConfig S_0071 = ThrottleConfig.of("S0071");

    ConfigLegacy conf;
    Socket socket;
    DataInputX in;
    DataOutputX out;
    int protocol;
    SocketAddress remoteAddr;
    long lastWriteTime;

    public LegacyTcpAgentWorker(ConfigLegacy configLegacy, Socket socket,
                                DataInputX in, DataOutputX out, int protocol) {

        this.conf = configLegacy;
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.protocol = protocol;
        this.remoteAddr = socket.getRemoteSocketAddress();
    }

    public void write(String cmd, Pack p) {
        if (socket.isClosed())
            return;

        try {
            switch(protocol) {
                case NetCafe.TCP_AGENT:
                    out.writeText(cmd);
                    out.writePack(p);
                case NetCafe.TCP_AGENT_V2:
                    byte[] buff = new DataOutputX().writeText(cmd).writePack(p).toByteArray();
                    out.writeIntBytes(buff);
                default:
            }
            out.flush();

            lastWriteTime = System.currentTimeMillis();

        } catch(Throwable t) {
            log.error(t.getMessage(), S_0067, t);
            close();
        }
    }

    public Pack readPack() {
        try {
            if (socket.isClosed()) {
                return null;
            } else {
                switch(protocol) {
                    case NetCafe.TCP_AGENT:
                        return in.readPack();
                    case NetCafe.TCP_AGENT_V2:
                        byte[] buff = in.readIntBytes();
                        return new DataInputX(buff).readPack();
                    default:
                        throw new RuntimeException("unknown protocol " );
                }
            }
        } catch(Throwable t) {
            log.error(t.getMessage(), S_0068, t);
            close();
        }
        return null;
    }

    public byte readByte() {
        try {
            if (socket.isClosed()) {
                return 0;
            }
            return in.readByte();

        } catch(Throwable t) {
            log.error(t.getMessage(), S_0069, t);
            close();
        }
        return 0;
    }

    public void read(TriProcedure<Integer, DataInputX, DataOutputX> handler) {
        try {
            handler.accept(protocol, in, out);

        } catch(Throwable t) {
            log.error(t.getMessage(), S_0070, t);
            close();
        }
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - lastWriteTime >= conf.getLegacyNetTcpAgentKeepaliveIntervalMs();
    }

    public void sendKeepAlive(int waitTime) {
        if (socket.isClosed())
            return;

        int orgSoTime = 0;
        try {
            orgSoTime = socket.getSoTimeout();
            write("KEEP_ALIVE", new MapPack());

            while (TcpFlag.HasNEXT == in.readByte()) {
                switch(protocol) {
                    case NetCafe.TCP_AGENT:
                        in.readPack();
                    case NetCafe.TCP_AGENT_V2:
                        in.readIntBytes();
                    default:
                }
            }
            socket.setSoTimeout(orgSoTime);

        } catch(Throwable t) {
            log.error(t.getMessage(), S_0071, t);
            close();
        }
    }

    public void close() {
        FileUtil.close(in);
        FileUtil.close(out);
        FileUtil.close(socket);
        if (conf.isLegacyLogTcpActionEnabled()) {
            log.info("Agent : {} closed", remoteAddr);
        }
    }
}
