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

import scouter.Version;
import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.lang.pack.MapPack;
import scouter2.collector.transport.legacy.service.annotation.LegacyServiceHandler;

import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-13
 */
public class LegacyServerInfoServiceHandler {
    
    @LegacyServiceHandler("SERVER_STATUS")
    public void getServerStatus(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;
        MapPack serverPack = new MapPack();
        serverPack.put("used", usedMemory);
        serverPack.put("total", totalMemory);
        serverPack.put("time", System.currentTimeMillis());
        dout.writeByte(3);
        dout.writePack(serverPack);
    }

    @LegacyServiceHandler("SERVER_VERSION")
    public void getServerVersion(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = din.readMapPack();

        MapPack serverPack = new MapPack();
        serverPack.put("version", Version.getServerFullVersion());
        dout.writeByte(3);
        dout.writePack(serverPack);
    }

//    @LegacyServiceHandler("SERVER_ENV")
//    public void getAgentEnv(DataInputX din, DataOutputX dout, boolean login) throws IOException {
//        final MapPack m = new MapPack();
//        final Properties p = System.getProperties();
//        JavaConversions..MODULE$.asScalaSet(p.keySet()).foreach(new AbstractFunction1()
//    {
//        public static final long serialVersionUID = 0L;
//
//        public final Value apply(Object key)
//        {
//            String value = p.getProperty(key.toString());
//            return m.put(key.toString(), value);
//        }
//    });
//        dout.writeByte(3);
//        dout.writePack(m);
//    }
//
//    @LegacyServiceHandler("SERVER_TIME")
//    public void getServerTime(DataInputX din, DataOutputX dout, boolean login) throws IOException {
//        MapPack serverPack = new MapPack();
//        serverPack.put("time", System.currentTimeMillis());
//        dout.writeByte(3);
//        dout.writePack(serverPack);
//    }
//
//    @LegacyServiceHandler("SERVER_LOG_LIST")
//    public void getServerLogs(DataInputX din, DataOutputX dout, boolean login) throws IOException {
//        MapPack out = new MapPack();
//        File logDir = new File(Configure.getInstance().log_dir);
//        if (logDir.exists() == false) {
//            return;
//        }
//        final ListValue nameLv = out.newList("name");
//        final ListValue sizeLv = out.newList("size");
//        final ListValue lastModifiedLv = out.newList("lastModified");
//        Predef..MODULE$.refArrayOps((Object[])logDir.listFiles()).foreach(new AbstractFunction1()
//    {
//        public static final long serialVersionUID = 0L;
//
//        public final void apply(File f)
//        {
//            if ((f.isFile()) && (f.getName().endsWith(".log")))
//            {
//                nameLv.add(f.getName());
//                sizeLv.add(f.length());
//                lastModifiedLv.add(f.lastModified());
//            }
//        }
//    });
//        dout.writeByte(3);
//        dout.writePack(out);
//    }
//
//    @LegacyServiceHandler("SERVER_LOG_DETAIL")
//    public void getServerLogDetail(DataInputX din, DataOutputX dout, boolean login) throws IOException {
//        MapPack param = din.readMapPack();
//        String name = param.getText("name");
//        if (StringUtil.isEmpty(name)) {
//            return;
//        }
//        File logFile = new File(Configure.getInstance().log_dir, name);
//        if (logFile.canRead() == false) {
//            return;
//        }
//        byte[] content = FileUtil.readAll(logFile);
//        if (content == null) {
//            return;
//        }
//        dout.writeByte(3);
//        dout.writeValue(new TextValue(new String(content)));
//    }
}
