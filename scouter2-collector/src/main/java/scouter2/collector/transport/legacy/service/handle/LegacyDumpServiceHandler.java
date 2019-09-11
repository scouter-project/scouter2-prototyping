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

package scouter2.collector.transport.legacy.service.handle;

import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.lang.pack.MapPack;
import scouter.net.RequestCmd;
import scouter.net.TcpFlag;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.springconfig.ApplicationContextHolder;
import scouter2.collector.transport.legacy.agent.LegacyAgentCall;
import scouter2.collector.transport.legacy.service.annotation.LegacyServiceHandler;

import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-13
 */
public class LegacyDumpServiceHandler {

    ObjService objService = ApplicationContextHolder.getBean(ObjService.class);
    LegacyAgentCall legacyAgentCall = ApplicationContextHolder.getBean(LegacyAgentCall.class);

    @LegacyServiceHandler(RequestCmd.TRIGGER_THREAD_DUMP)
    public void triggerThreadDump(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = din.readMapPack();
        int objId = param.getInt("objHash");
        Obj obj = objService.findById(objId);
        MapPack pack = legacyAgentCall.call(obj, RequestCmd.TRIGGER_THREAD_DUMP, param);
        if (pack != null) {
            dout.writeByte(TcpFlag.HasNEXT);
            dout.writePack(pack);
        }
    }

    @LegacyServiceHandler(RequestCmd.TRIGGER_THREAD_LIST)
    public void triggerThreadList(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = din.readMapPack();
        int objId = param.getInt("objHash");
        Obj obj = objService.findById(objId);
        MapPack pack = legacyAgentCall.call(obj, RequestCmd.TRIGGER_THREAD_LIST, param);
        if (pack != null) {
            dout.writeByte(TcpFlag.HasNEXT);
            dout.writePack(pack);
        }
    }

    @LegacyServiceHandler(RequestCmd.OBJECT_THREAD_LIST)
    public void agentThreadList(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = din.readMapPack();
        int objId = param.getInt("objHash");
        Obj obj = objService.findById(objId);
        MapPack pack = legacyAgentCall.call(obj, RequestCmd.OBJECT_THREAD_LIST, param);
        if (pack != null) {
            dout.writeByte(TcpFlag.HasNEXT);
            dout.writePack(pack);
        }
    }
}
