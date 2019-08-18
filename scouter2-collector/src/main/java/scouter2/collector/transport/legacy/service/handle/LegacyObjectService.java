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

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-13
 */
public class LegacyObjectService {

    /**
     * OBJECT_LIST_REAL_TIME
     * @param din none
     * @param dout ObjectPack[]
     * @param login
     */
//    @LegacyServiceHandler(RequestCmd.OBJECT_LIST_REAL_TIME)
//    public void agentList(DataInputX din, DataOutputX dout, boolean login) {
//
//        val engine = CounterManager.getInstance().getCounterEngine();
//        val en: java.util.Enumeration[ObjectPack] = AgentManager.getObjPacks();
//        while (en.hasMoreElements()) {
//            val p = en.nextElement()
//            val ac = engine.getMasterCounter(p.objType);
//            try {
//                if (p.alive) {
//                    val c = CounterCache.get(new CounterKey(p.objHash, ac, TimeTypeEnum.REALTIME));
//                    if (c != null) {
//                        p.tags.put("counter", c);
//                    }
//                }
//            } catch {
//                case e: Throwable => { e.printStackTrace() }
//            }
//            dout.writeByte(TcpFlag.HasNEXT);
//            dout.writePack(p);
//        }
//    }
//
//    @LegacyServiceHandler("OBJECT_LIST_LOAD_DATE")
//    public void getAgentOldList(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//        String date = param.getText("date");
//        if (StringUtil.isEmpty(date)) {
//            return;
//        }
//        MapPack mpack = ObjectRD..MODULE$.getDailyAgent(date);
//        dout.writeByte(3);
//        dout.writePack(mpack);
//    }
//
//
//    @LegacyServiceHandler("OBJECT_REMOVE_INACTIVE")
//    public void agentRemoveInactive(DataInputX din, DataOutputX dout, boolean login)
//    {
//        AgentManager..MODULE$.clearInactive();
//    }
//
//    @LegacyServiceHandler("OBJECT_INFO")
//    public void getAgentInfo(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//        String date = param.getText("date");
//        int objHash = param.getInt("objHash");
//        if (StringUtil.isEmpty(date)) {
//            date = DateUtil.yyyymmdd();
//        }
//        ObjectPack objPack = ObjectRD..MODULE$.getObjectPack(date, objHash);
//        dout.writeByte(3);
//        dout.writePack(objPack);
//    }
//
//    @LegacyServiceHandler("OBJECT_ENV")
//    public void agentEnv(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//        int objHash = param.getInt("objHash");
//
//        ObjectPack agent = AgentManager..MODULE$.getAgent(objHash);
//
//        MapPack p = AgentCall..MODULE$.call(agent, "OBJECT_ENV", param);
//        if (p != null)
//        {
//            dout.writeByte(3);
//            dout.writePack(p);
//        }
//    }
//
//    @LegacyServiceHandler("OBJECT_CLASS_DESC")
//    public void getClassDesc(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//        int objHash = param.getInt("objHash");
//        ObjectPack o = AgentManager..MODULE$.getAgent(objHash);
//
//        MapPack p = AgentCall..MODULE$.call(o, "OBJECT_CLASS_DESC", param);
//        if (p != null)
//        {
//            dout.writeByte(3);
//            dout.writePack(p);
//        }
//    }
//
//    @LegacyServiceHandler("OBJECT_CLASS_LIST")
//    public void getLoadedClassList(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//        int objHash = param.getInt("objHash");
//
//        ObjectPack o = AgentManager..MODULE$.getAgent(objHash);
//
//        MapPack p = AgentCall..MODULE$.call(o, "OBJECT_CLASS_LIST", param);
//        if (p != null)
//        {
//            dout.writeByte(3);
//            dout.writePack(p);
//        }
//    }
//
//    @LegacyServiceHandler("OBJECT_RESET_CACHE")
//    public void resetAgentCache(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//
//        int objHash = param.getInt("objHash");
//
//        ObjectPack o = AgentManager..MODULE$.getAgent(objHash);
//
//        AgentCall..MODULE$.call(o, "OBJECT_RESET_CACHE", param);
//    }
//
//    @LegacyServiceHandler("OBJECT_REMOVE")
//    public void agentRemove(DataInputX din, DataOutputX dout, boolean login)
//    {
//        final ListValue objHashList = (ListValue)din.readValue();
//        final ArrayList removeObjList = new ArrayList(objHashList.size());RichInt..MODULE$
//            .to$extension0(Predef..MODULE$.intWrapper(0), objHashList.size() - 1).foreach(new AbstractFunction1.mcZI.sp()
//    {
//        public static final long serialVersionUID = 0L;
//
//        public final boolean apply(int i)
//        {
//            return apply$mcZI$sp(i);
//        }
//
//        public boolean apply$mcZI$sp(int i)
//        {
//            int objHash = (int)objHashList.getLong(i);
//            return removeObjList.add(BoxesRunTime.boxToInteger(objHash));
//        }
//    });
//        AgentManager..MODULE$.removeAgents(removeObjList, true);
//        getFullAgentList(din, dout, login);
//    }
//
//    @LegacyServiceHandler("OBJECT_TODAY_FULL_LIST")
//    public void getFullAgentList(DataInputX din, final DataOutputX dout, boolean login)
//    {
//        String date = DateUtil.yyyymmdd();
//        List objectList = ObjectRD..MODULE$.getObjectList(date);
//        JavaConversions..MODULE$.asScalaBuffer(objectList).foreach(new AbstractFunction1()
//    {
//        public static final long serialVersionUID = 0L;
//
//        public final void apply(ObjectPack pack)
//        {
//            ObjectPack inMemory = AgentManager..MODULE$.getAgent(pack.objHash);
//            if (inMemory == null)
//            {
//                pack.tags.put("status", "dead");
//                pack.alive = false;
//                dout.writeByte(3);
//                dout.writePack(pack);
//                dout.flush();
//            }
//            else
//            {
//                (inMemory.alive ?
//                        inMemory.tags.put("status", "active") :
//
//                        inMemory.tags.put("status", "inactive"));
//
//                dout.writeByte(3);
//                dout.writePack(inMemory);
//                dout.flush();
//            }
//        }
//    });
//    }
//
//    @LegacyServiceHandler("OBJECT_REMOVE_IN_MEMORY")
//    public void agentRemoveInMemory(DataInputX din, DataOutputX dout, boolean login)
//    {
//        final ListValue objHashList = (ListValue)din.readValue();
//        final ArrayList removeObjList = new ArrayList(objHashList.size());RichInt..MODULE$
//            .to$extension0(Predef..MODULE$.intWrapper(0), objHashList.size() - 1).foreach(new AbstractFunction1.mcZI.sp()
//    {
//        public static final long serialVersionUID = 0L;
//
//        public final boolean apply(int i)
//        {
//            return apply$mcZI$sp(i);
//        }
//
//        public boolean apply$mcZI$sp(int i)
//        {
//            int objHash = (int)objHashList.getLong(i);
//            return removeObjList.add(BoxesRunTime.boxToInteger(objHash));
//        }
//    });
//        AgentManager..MODULE$.removeAgents(removeObjList, false);
//        getFullAgentList(din, dout, login);
//    }
//
//    @LegacyServiceHandler("OBJECT_HEAPHISTO")
//    public void agentObjectHeapHisto(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//        int objHash = param.getInt("objHash");
//
//        ObjectPack o = AgentManager..MODULE$.getAgent(objHash);
//
//        MapPack p = AgentCall..MODULE$.call(o, "OBJECT_HEAPHISTO", param);
//        if (p != null)
//        {
//            dout.writeByte(3);
//            dout.writePack(p);
//        }
//    }
//
//    @LegacyServiceHandler("OBJECT_FILE_SOCKET")
//    public void agentObjectFileSocket(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//        int objHash = param.getInt("objHash");
//
//        ObjectPack o = AgentManager..MODULE$.getAgent(objHash);
//
//        MapPack p = AgentCall..MODULE$.call(o, "OBJECT_FILE_SOCKET", param);
//        if (p != null)
//        {
//            dout.writeByte(3);
//            dout.writePack(p);
//        }
//    }
//
//    @LegacyServiceHandler("SOCKET")
//    public void agentObjectSocket(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//        int objHash = param.getInt("objHash");
//
//        ObjectPack o = AgentManager..MODULE$.getAgent(objHash);
//
//        MapPack p = AgentCall..MODULE$.call(o, "SOCKET", param);
//        if (p != null)
//        {
//            dout.writeByte(3);
//            dout.writePack(p);
//        }
//    }
//
//    @LegacyServiceHandler("OBJECT_LOAD_CLASS_BY_STREAM")
//    public void loadClassStream(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//        int objHash = param.getInt("objHash");
//        ObjectPack o = AgentManager..MODULE$.getAgent(objHash);
//        MapPack p = AgentCall..MODULE$.call(o, "OBJECT_LOAD_CLASS_BY_STREAM", param);
//        if (p != null)
//        {
//            dout.writeByte(3);
//            dout.writePack(p);
//        }
//    }
//
//    @LegacyServiceHandler("REDIS_INFO")
//    public void redisInfo(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//        int objHash = param.getInt("objHash");
//        ObjectPack o = AgentManager..MODULE$.getAgent(objHash);
//        MapPack p = AgentCall..MODULE$.call(o, "REDIS_INFO", param);
//        if (p != null)
//        {
//            dout.writeByte(3);
//            dout.writePack(p);
//        }
//    }
//
//    @LegacyServiceHandler("OBJECT_CHECK_RESOURCE_FILE")
//    public void checkJarFile(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//        int objHash = param.getInt("objHash");
//        ObjectPack o = AgentManager..MODULE$.getAgent(objHash);
//        MapPack p = AgentCall..MODULE$.call(o, "OBJECT_CHECK_RESOURCE_FILE", param);
//        if (p != null)
//        {
//            dout.writeByte(3);
//            dout.writePack(p);
//        }
//    }
//
//    @LegacyServiceHandler("OBJECT_DOWNLOAD_JAR")
//    public void downloadJar(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack param = (MapPack)din.readPack();
//        int objHash = param.getInt("objHash");
//        ObjectPack o = AgentManager..MODULE$.getAgent(objHash);
//        MapPack p = AgentCall..MODULE$.call(o, "OBJECT_DOWNLOAD_JAR", param);
//        if (p != null)
//        {
//            dout.writeByte(3);
//            dout.writePack(p);
//        }
//    }
}
