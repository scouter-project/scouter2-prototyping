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

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.multimap.list.MutableListMultimap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.LongSets;
import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.lang.pack.MapPack;
import scouter.lang.pack.ObjectPack;
import scouter.lang.value.DoubleValue;
import scouter.lang.value.ListValue;
import scouter.net.RequestCmd;
import scouter.net.TcpFlag;
import scouter.util.StringUtil;
import scouter2.collector.domain.metric.MetricService;
import scouter2.collector.domain.metric.SingleMetricDatum;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.domain.objtype.ObjFamilyManager;
import scouter2.collector.legacy.LegacySupport;
import scouter2.collector.springconfig.ApplicationContextHolder;
import scouter2.collector.transport.legacy.LegacyMapper;
import scouter2.collector.transport.legacy.service.annotation.LegacyServiceHandler;

import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-13
 */
public class LegacyObjectServiceHandler {

    ObjService objService = ApplicationContextHolder.getBean(ObjService.class);
    MetricService metricService = ApplicationContextHolder.getBean(MetricService.class);

    /**
     * OBJECT_LIST_REAL_TIME
     * @param din none
     * @param dout ObjectPack[]
     * @param login
     */
    @LegacyServiceHandler(RequestCmd.OBJECT_LIST_REAL_TIME)
    public void findObjList(DataInputX din, DataOutputX dout, boolean login) throws IOException {

        MutableList<Obj> objs = Lists.adapt(objService
                .findByApplicationId(LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT));

        MutableLongSet objIds = LongSets.mutable.ofAll(Lists.adapt(objs).collectLong(Obj::getObjId));
        MutableSet<String> metricsIds = objs
                .collect(obj -> ObjFamilyManager.getInstance().getMasterMetricName(obj.getObjFamily())).toSet();

        MutableListMultimap<Long, SingleMetricDatum> metricsOfObj = metricService
                .findCurrentMetricData(LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT, objIds, metricsIds)
                .groupBy(SingleMetricDatum::getObjId);

        for (Obj obj : objs) {
            ObjectPack pack = LegacyMapper.toObjectPack(obj);

            if (!objService.isDeadObject(obj)) {
                String masterMetricName = ObjFamilyManager.getInstance().getMasterMetricName(obj.getObjFamily());
                long masterMetricId = metricService.findMetricIdAbsentGen(masterMetricName);

                //TODO getMasterMetricDefs 타입에 따라 float or decimal value
                //TODO 특이케이스 어캐할지를 결정. heap, active servier
                MutableList<SingleMetricDatum> metricsByObj = metricsOfObj.get(obj.getObjId());
                metricsByObj.select(metric -> masterMetricId == metric.getMetricId()).getFirstOptional()
                        .ifPresent(datum -> pack.tags.put("counter", new DoubleValue(datum.getValue())));
            }

            dout.writeByte(TcpFlag.HasNEXT);
            dout.writePack(pack);
        }
    }

    @LegacyServiceHandler("OBJECT_LIST_LOAD_DATE")
    public void getAgentOldList(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        //TODO obj of date. Currently return all realtime obj for testing !!!
        MapPack param = (MapPack)din.readPack();
        String date = param.getText("date");
        if (StringUtil.isEmpty(date)) {
            return;
        }

        MutableList<Obj> objs = Lists.adapt(objService
                .findByApplicationId(LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT));

        MapPack m = new MapPack();
        ListValue objTypeLv = m.newList("objType");
        ListValue objHashLv = m.newList("objHash");
        ListValue objNameLv = m.newList("objName");

        for (Obj obj : objs) {
            ObjectPack pack = LegacyMapper.toObjectPack(obj);
            objTypeLv.add(pack.objType);
            objHashLv.add(pack.objHash);
            objNameLv.add(pack.objName);
        }
        dout.writeByte(TcpFlag.HasNEXT);
        dout.writePack(m);
    }
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
