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
import org.springframework.stereotype.Component;
import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.lang.pack.MapPack;
import scouter.lang.pack.Pack;
import scouter.lang.pack.PackEnum;
import scouter.net.TcpFlag;
import scouter2.collector.common.functional.TriProcedure;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.domain.obj.Obj;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 11/09/2019
 */
@Component
@Slf4j
public class LegacyAgentCall {

    static final ThrottleConfig S_0072 = ThrottleConfig.of("S0072");
    static final ThrottleConfig S_0073 = ThrottleConfig.of("S0073");
    static final ThrottleConfig S_0074 = ThrottleConfig.of("S0074");

    LegacyTcpAgentManager agentManager;

    public LegacyAgentCall(LegacyTcpAgentManager agentManager) {
        this.agentManager = agentManager;
    }

    public MapPack call(Obj obj, String cmd, MapPack param) {
        if (obj == null) {
            log.warn("Agent call error: obj is null.", S_0072);
        }
        if (obj.getObjLegacyHash() == 0) {
            log.warn("Agent call error: obj hash is zero. non legacy obj?", S_0072);
        }

        LegacyTcpAgentWorker tcpAgent = agentManager.get(obj.getObjLegacyHash());
        if (tcpAgent == null || tcpAgent.isClosed()) {
            log.warn("Can not find a legacy tcp agent for {}", obj.getObjFullName(), S_0073);
        }
        try {
            tcpAgent.write(cmd, param != null ? param : new MapPack());
            Pack pack = null;
            while (tcpAgent.readByte() == TcpFlag.HasNEXT) {
                pack = tcpAgent.readPack();
            }
            if (pack == null) {
                return null;
            }
            if (pack.getPackType() == PackEnum.MAP) {
                return (MapPack) pack;
            } else {
                log.warn("Not allowed legacy agent call response : {}", pack, S_0074);
            }
        } finally {
            agentManager.add(obj.getObjLegacyHash(), tcpAgent);
        }

        return null;
    }

    public void call(Obj obj, String cmd, MapPack param, TriProcedure<Integer, DataInputX, DataOutputX> handler) {
        if (obj == null) {
            log.warn("Agent call error: obj is null.", S_0072);
        }
        if (obj.getObjLegacyHash() == 0) {
            log.warn("Agent call error: obj hash is zero. non legacy obj?", S_0072);
        }

        LegacyTcpAgentWorker tcpAgent = agentManager.get(obj.getObjLegacyHash());
        if (tcpAgent == null || tcpAgent.isClosed()) {
            log.warn("Can not find a legacy tcp agent for {}", obj.getObjFullName(), S_0073);
        }
        try {
            tcpAgent.write(cmd, param != null ? param : new MapPack());
            tcpAgent.read(handler);
        } finally {
            agentManager.add(obj.getObjLegacyHash(), tcpAgent);
        }
    }
}
