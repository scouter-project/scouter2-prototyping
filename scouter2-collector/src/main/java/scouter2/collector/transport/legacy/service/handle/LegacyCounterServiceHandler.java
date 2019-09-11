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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.collections.impl.factory.primitive.LongObjectMaps;
import org.eclipse.collections.impl.factory.primitive.LongSets;
import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.lang.pack.MapPack;
import scouter.lang.value.DoubleValue;
import scouter.lang.value.ListValue;
import scouter.lang.value.Value;
import scouter.net.RequestCmd;
import scouter.net.TcpFlag;
import scouter.util.StringUtil;
import scouter2.collector.domain.metric.MetricService;
import scouter2.collector.domain.metric.SingleMetricDatum;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.legacy.LegacySupport;
import scouter2.collector.transport.legacy.service.annotation.LegacyServiceHandler;
import scouter2.proto.TimeTypeP;

import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-23
 */
@Slf4j
public class LegacyCounterServiceHandler {

    private MetricService metricService = MetricService.getInstance();
    private ObjService objService = ObjService.getInstance();

    @LegacyServiceHandler(RequestCmd.COUNTER_REAL_TIME)
    public void getRealTime(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = (MapPack) din.readPack();
        int objHash = param.getInt("objHash");
        String counter = param.getText("counter");

        MutableList<SingleMetricDatum> metrics = metricService.findCurrentMetricData(
                LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT,
                LongSets.mutable.of(objHash),
                Sets.mutable.of(counter));

        Value v = metrics.getFirstOptional().map(d -> new DoubleValue(d.getValue())).orElse(null);
        if (v != null) {
            dout.writeByte(TcpFlag.HasNEXT);
            dout.writeValue(v);
        }
    }

    /**
     * get a latest counter value for specific object type
     *
     * @param din   MapPack{counter, objType}
     * @param dout  MapPack{objHash[], value[]}
     * @param login
     */
    @LegacyServiceHandler(RequestCmd.COUNTER_REAL_TIME_ALL)
    public void getRealTimeAll(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = (MapPack) din.readPack();
        String counter = param.getText("counter");
        String objType = param.getText("objType");

        if (StringUtil.isEmpty(objType)) {
            log.warn("please check.. COUNTER_REAL_TIME_ALL objType is null");
            return;
        }

        MutableList<Obj> objs = objService.findByObjTypeParam(objType);
        MutableList<SingleMetricDatum> metrics = metricService.findCurrentMetricData(
                LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT,
                objs.collectLong(Obj::getObjId).toSet(),
                Sets.mutable.of(counter));

        MapPack mpack = new MapPack();
        ListValue instList = mpack.newList("objHash");
        ListValue values = mpack.newList("value");

        for (SingleMetricDatum metric : metrics) {
            Value v = new DoubleValue(metric.getValue());
            instList.add(metric.getObjId());
//            instList.add(objMap.get(metric.getObjId()).getObjLegacyHash());
            values.add(v);
        }
        dout.writeByte(TcpFlag.HasNEXT);
        dout.writePack(mpack);
    }

    @LegacyServiceHandler(RequestCmd.COUNTER_PAST_TIME)
    public void getPastTime(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = (MapPack) din.readPack();
        String counter = param.getText("counter");
        int objId = param.getInt("objHash");

        long stime = param.getLong("stime");
        long etime = param.getLong("etime");

        MapPack mpack = new MapPack();
        ListValue timeLv = mpack.newList("time");
        ListValue valueLv = mpack.newList("value");

        long metricId = metricService.findMetricIdAbsentGen(counter);
        metricService.streamListByObjs(
                LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT,
                LongSets.mutable.of(objId),
                stime,
                etime,
                TimeTypeP.REALTIME,
                metric -> {
                        Double value = metric.getMetricsMap().get(metricId);
                        if (value != null) {
                            timeLv.add(metric.getTimestamp());
                            valueLv.add(value);
                        }
                    });

        dout.writeByte(TcpFlag.HasNEXT);
        dout.writePack(mpack);
    }

    /**
     * get a counter's values for specific object type or hashes
     *
     * @param din   MapPack{stime, etime, counter, (objType or objHashLv)}
     * @param dout  MapPack{objHash[], time[], value[]}
     * @param login
     */
    @LegacyServiceHandler(RequestCmd.COUNTER_PAST_TIME_ALL)
    public void getPastTimeAll(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = (MapPack) din.readPack();
        String counter = param.getText("counter");
        long stime = param.getLong("stime");
        long etime = param.getLong("etime");
        String objType = param.getText("objType");
        ListValue objHashParamLv = param.getList("objHash");

        if (StringUtils.isBlank(objType) && (objHashParamLv == null || objHashParamLv.size() == 0)) {
            log.warn("please check.. COUNTER_LOAD_TIME_ALL objType is null");
            return;
        }

        MutableLongList objIds;
        if (objHashParamLv != null && objHashParamLv.size() > 0) {
            objIds = LegacySupport.listValue2LongList(objHashParamLv);
        } else {
            objIds = objService.findByObjTypeParam(objType).collectLong(Obj::getObjId);
        }

        MutableLongObjectMap<MapPack> mapPackMap = LongObjectMaps.mutable.empty();
        objIds.forEach(objId -> {
            MapPack mapPack = new MapPack();
            mapPack.put("objHash", objId);
            mapPack.newList("time");
            mapPack.newList("value");
            mapPackMap.put(objId, mapPack);
        });

        long metricId = metricService.findMetricIdAbsentGen(counter);
        metricService.streamListByObjs(
                LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT,
                objIds.toSet(),
                stime,
                etime,
                TimeTypeP.REALTIME,
                metric -> {
                    Double value = metric.getMetricsMap().get(metricId);
                    if (value != null) {
                        MapPack mapPackOfObj = mapPackMap.get(metric.getObjId());
                        if (mapPackOfObj != null) {
                            mapPackOfObj.getList("time").add(metric.getTimestamp());
                            mapPackOfObj.getList("value").add(value);
                        }
                    }
                });

        for (int i = 0; i < objIds.size(); i++) {
            MapPack mapPackOfObj = mapPackMap.get(objIds.get(i));
            dout.writeByte(TcpFlag.HasNEXT);
            if (mapPackOfObj != null) {
                dout.writePack(mapPackOfObj);
                dout.flush();
            }
        }
    }

//    @LegacyServiceHandler(RequestCmd.COUNTER_REAL_TIME_TOT)
//    public void getRealTimeTotal(DataInputX din, DataOutputX dout, boolean login) {
//
//    }
//        val inout = din.readPack().asInstanceOf[MapPack];
//        val objType = inout.getText("objType");
//        if (StringUtil.isEmpty(objType)) {
//            System.out.println("please check.. COUNTER_REAL_TIME_TOT objType is null");
//            return ;
//        }
//        val counter = inout.getText("counter");
//        val mode = inout.getText("mode");
//
//        var vvv = 0.0;
//        var cnt = 0;
//        val insList = AgentManager.getLiveObjHashList(objType);
//        for (objHash <- insList) {
//            val key = new CounterKey(objHash, counter, TimeTypeEnum.REALTIME);
//            val v = CounterCache.get(key);
//            if (v != null) {
//                vvv += CastUtil.cdouble(v);
//                cnt += 1;
//            }
//        }
//        if (cnt > 0 && "avg".equalsIgnoreCase(mode)) {
//            vvv = vvv / cnt;
//        }
//        dout.writeByte(TcpFlag.HasNEXT);
//        dout.writeValue(new DoubleValue(vvv));
//    }
//

//
//    def getObjName(date: String, objHash: Int): String = {
//        return ObjectRD.getObjName(date, objHash);
//    }
//
//    @ServiceHandler(RequestCmd.COUNTER_PAST_TIME_TOT)
//    def getPastTimeTotal(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readPack().asInstanceOf[MapPack];
//        val stime = param.getLong("stime");
//        val etime = param.getLong("etime");
//        val counter = param.getText("counter");
//        val objType = param.getText("objType");
//
//        if (StringUtil.isEmpty(objType)) {
//            System.out.println("please check.. COUNTER_PAST_TIME_TOT objType is null");
//            return ;
//        }
//
//        val mode = param.getText("mode");
//        val date = DateUtil.yyyymmdd(stime);
//
//        val agentGrp = AgentManager.getDailyObjects(date, objType);
//        val objHashLv = agentGrp.getList("objHash");
//
//        val series = new TimedSeries[Integer, Double]();
//
//        for (i <- 0 to objHashLv.size() - 1) {
//            val objHash = objHashLv.getInt(i);
//            val objName = getObjName(date, objHash);
//            /////////////////RealtimeCounterRD////////////////////////
//            val handler = (time: Long, data:MapValue) => {
//                val value = data.get(counter);
//                if (value != null) {
//                    series.add(objHash, time, CastUtil.cdouble(value));
//                }
//                true
//            }
//
//            RealtimeCounterRD.read(objName, date, stime, etime, handler)
//        }
//        series.addEnd();
//
//        val mpack = new MapPack();
//        val timeLv = mpack.newList("time");
//        val valueLv = mpack.newList("value");
//
//        var minTime = series.getMinTime();
//        val maxTime = series.getMaxTime();
//        while (minTime <= maxTime) {
//            var sum = 0.0d;
//            val list = series.getInTimeList(minTime, 10000);
//            for (i <- list) {
//                sum += i;
//            }
//            timeLv.add(minTime);
//            if ("avg".equals(mode)) {
//                if (list.size() > 0) {
//                    valueLv.add(sum / list.size());
//                } else {
//                    valueLv.add(0);
//                }
//            } else {
//                valueLv.add(sum);
//            }
//            minTime += (DateUtil.MILLIS_PER_SECOND * 2);
//        }
//
//        dout.writeByte(TcpFlag.HasNEXT);
//        dout.writePack(mpack);
//        dout.flush();
//    }

//    @ServiceHandler(RequestCmd.COUNTER_REAL_TIME_MULTI)
//    def getRealTimeMulti(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readPack().asInstanceOf[MapPack];
//        val objHash = param.getInt("objHash");
//        val counterLv = param.getList("counter");
//
//        val out = new MapPack();
//        val counterOut = out.newList("counter");
//        val valueOut = out.newList("value");
//
//        for (i <- 0 to counterLv.size() - 1) {
//            val key = new CounterKey(objHash, counterLv.getString(i), TimeTypeEnum.REALTIME);
//            val v = CounterCache.get(key);
//            if (v != null) {
//                counterOut.add(counterLv.get(i));
//                valueOut.add(v);
//            }
//        }
//        dout.writeByte(TcpFlag.HasNEXT);
//        dout.writePack(out);
//    }

//    @ServiceHandler(RequestCmd.COUNTER_REAL_TIME_OBJECT_ALL)
//    def getRealTimeObjectAll(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readPack().asInstanceOf[MapPack];
//        val objHash = param.getInt("objHash");
//
//        val cntMap = CounterCache.getObjectCounters(objHash, TimeTypeEnum.REALTIME);
//
//        val mpack = new MapPack();
//        val counters = mpack.newList("counter");
//        val values = mpack.newList("value");
//
//        for (counter <- cntMap.keySet()) {
//            val value = cntMap.get(counter);
//            counters.add(counter);
//            values.add(value);
//        }
//
//        dout.writeByte(TcpFlag.HasNEXT);
//        dout.writePack(mpack);
//    }
//
//    @ServiceHandler(RequestCmd.COUNTER_REAL_TIME_OBJECT_TYPE_ALL)
//    def getRealTimeObjectTypeAll(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readPack().asInstanceOf[MapPack];
//        val objType = param.getText("objType");
//
//        val liveObjectList = AgentManager.getLiveObjHashList(objType);
//        val mpack = new MapPack();
//        for (objHash <- liveObjectList) {
//            val mapValue = new MapValue();
//            val cntMap = CounterCache.getObjectCounters(objHash.intValue(), TimeTypeEnum.REALTIME);
//            for (counter <- cntMap.keySet()) {
//                val value = cntMap.get(counter);
//                mapValue.put(counter, value);
//                mpack.put(AgentManager.getAgentName(objHash.intValue()), mapValue);
//            }
//        }
//        dout.writeByte(TcpFlag.HasNEXT);
//        dout.writePack(mpack);
//    }
//
//    /**
//     * get latest several counter's values for specific object type
//     * @param din MapPack{counter[], (objType or objHashLv)}
//     * @param dout MapPack{objHash[], counter[], value[]}
//     * @param login
//     */
//    @ServiceHandler(RequestCmd.COUNTER_REAL_TIME_ALL_MULTI)
//    def getRealTimeAllMulti(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readPack().asInstanceOf[MapPack];
//        val counters = param.getList("counter");
//        val objType = param.getText("objType");
//        val objHashLv = param.getList("objHash");
//
//        if (StringUtil.isEmpty(objType) && (objHashLv == null || objHashLv.size() == 0)) {
//            System.out.println("please check.. COUNTER_REAL_TIME_ALL objType is null");
//            return ;
//        }
//        var insts = AgentManager.getLiveObjHashList(objType);
//        if(objHashLv != null && objHashLv.size() > 0) {
//            insts.clear()
//            for( i <- 0 to objHashLv.size() - 1) {
//                insts.add(objHashLv.getInt(i))
//            }
//        }
//
//        val mpack = new MapPack();
//        val instList = mpack.newList("objHash");
//        val counterList = mpack.newList("counter");
//        val valueList = mpack.newList("value");
//
//        for(objHash <- insts) {
//            for( i <- 0 to counters.size() - 1) {
//                val key =  new CounterKey(objHash, counters.getString(i), TimeTypeEnum.REALTIME);
//                val value = CounterCache.get(key);
//                if(value != null) {
//                    instList.add(objHash);
//                    counterList.add(counters.get(i));
//                    valueList.add(value);
//                }
//            }
//        }
//        dout.writeByte(TcpFlag.HasNEXT);
//        dout.writePack(mpack);
//    }
//

//    @ServiceHandler(RequestCmd.COUNTER_PAST_TIME_GROUP)
//    def getPastTimeGroupAll(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readPack().asInstanceOf[MapPack];
//        val objHashLv = param.getList("objHash");
//        val counter = param.getText("counter");
//        val stime = param.getLong("stime");
//        val etime = param.getLong("etime");
//        val date = DateUtil.yyyymmdd(stime);
//        //#############################################
////        var start = System.currentTimeMillis()
//        val mapPackMap = new IntKeyMap[MapPack]();
//        for (i <- 0 to objHashLv.size() - 1) {
//            val objHash = objHashLv.getInt(i);
//            val mapPack = new MapPack();
//            mapPack.put("objHash", objHash);
//            val timeLv = mapPack.newList("time");
//            val valueLv = mapPack.newList("value");
//            mapPackMap.put(objHash, mapPack);
//        }
//
//        val handler = (mapValue: MapValue) => {
//            if (mapValue != null) {
//                val objHash = mapValue.getInt(CounterConstants.COMMON_OBJHASH)
//                val time = mapValue.getLong(CounterConstants.COMMON_TIME)
//                val value = mapValue.get(counter)
//                if(value != null) {
//                    val curMapPack = mapPackMap.get(objHash)
//                    if(curMapPack != null) {
//                        curMapPack.getList("time").add(time)
//                        curMapPack.getList("value").add(value)
//                    }
//                }
//            }
//        }
//
//        RealtimeCounterRD.readBulk(date, stime, etime, handler)
//
//        for (i <- 0 to objHashLv.size() - 1) {
//            dout.writeByte(TcpFlag.HasNEXT);
//            var mpack = mapPackMap.get(objHashLv.getInt(i))
////            System.out.println(mpack)
//            dout.writePack(mpack)
//            dout.flush()
//        }
//
//
//    }
//
//    @ServiceHandler(RequestCmd.COUNTER_REAL_TIME_GROUP)
//    def getRealTimeGroupAll(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readPack().asInstanceOf[MapPack];
//        val counter = param.getText("counter");
//        val objHashLv = param.getList("objHash");
//
//        val mpack = new MapPack();
//        val instList = mpack.newList("objHash");
//        val values = mpack.newList("value");
//
//        for (i <- 0 to objHashLv.size() - 1) {
//            val key = new CounterKey(objHashLv.getInt(i), counter, TimeTypeEnum.REALTIME);
//            val v = CounterCache.get(key);
//            if (v != null) {
//                instList.add(objHashLv.get(i));
//                values.add(v);
//            }
//        }
//
//        dout.writeByte(TcpFlag.HasNEXT);
//        dout.writePack(mpack);
//    }

}
