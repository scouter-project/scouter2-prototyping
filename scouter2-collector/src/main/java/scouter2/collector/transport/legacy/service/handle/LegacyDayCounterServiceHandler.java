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

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.MutableLongList;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.impl.factory.primitive.LongObjectMaps;
import org.eclipse.collections.impl.factory.primitive.LongSets;
import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.lang.pack.MapPack;
import scouter.lang.value.ListValue;
import scouter.net.RequestCmd;
import scouter.net.TcpFlag;
import scouter.util.StringUtil;
import scouter2.collector.common.util.U;
import scouter2.collector.domain.metric.MetricService;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.legacy.LegacySupport;
import scouter2.collector.transport.legacy.service.annotation.LegacyServiceHandler;
import scouter2.common.util.DateUtil;
import scouter2.proto.Metric4RepoP;
import scouter2.proto.TimeTypeP;

import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-23
 */
@Slf4j
public class LegacyDayCounterServiceHandler {

    private MetricService metricService = MetricService.getInstance();
    private ObjService objService = ObjService.getInstance();

    @LegacyServiceHandler(RequestCmd.COUNTER_TODAY)
    public void getDailyCounter(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = din.readMapPack();
        int objId = param.getInt("objHash");
        String counter = param.getText("counter");
        TimeTypeP timeType = TimeTypeP.forNumber(param.getInt("timetype"));

        String date = DateUtil.yyyymmdd();
        long stime = DateUtil.yyyymmdd(date);
        long etime = U.now();

        MapPack mpack = new MapPack();
        ListValue timeLv = mpack.newList("time");
        ListValue valueLv = mpack.newList("value");

        metricService.streamListByObjs(
                LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT,
                LongSets.mutable.of(objId),
                stime,
                etime,
                timeType,
                new StreamObserver<Metric4RepoP>() {
                    @Override
                    public void onNext(Metric4RepoP metric) {
                        Double value = metric.getMetricsMap().get(metricService.findMetricIdAbsentGen(counter));
                        if (value != null) {
                            timeLv.add(metric.getTimestamp());
                            valueLv.add(value);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        throw new RuntimeException(t);
                    }

                    @Override
                    public void onCompleted() {
                        try {
                            dout.writeByte(TcpFlag.HasNEXT);
                            dout.writePack(mpack);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    @LegacyServiceHandler(RequestCmd.COUNTER_TODAY_ALL)
    public void getDailyCounterAll(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = din.readMapPack();

        String counter = param.getText("counter");
        TimeTypeP timeType = TimeTypeP.FIVE_MIN;
        String objType = param.getText("objType");

        if (StringUtil.isEmpty(objType)) {
            log.warn("please check.. COUNTER_REAL_TIME_ALL objType is null");
            return;
        }
        MutableList<Obj> objs = objService.findByLegacyObjType(objType);
        MutableLongList objIds = objs.collectLong(Obj::getObjId);

        String date = DateUtil.yyyymmdd();
        long stime = DateUtil.yyyymmdd(date);
        long etime = U.now();

        MutableLongObjectMap<MapPack> mapPackMap = LongObjectMaps.mutable.empty();
        objIds.forEach(objId -> {
            MapPack mapPack = new MapPack();
            mapPack.put("objHash", objId);
            mapPack.newList("time");
            mapPack.newList("value");
            mapPackMap.put(objId, mapPack);
        });

        metricService.streamListByObjs(
                LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT,
                objIds.toSet(),
                stime,
                etime,
                timeType,
                new StreamObserver<Metric4RepoP>() {
                    @Override
                    public void onNext(Metric4RepoP metric) {
                        Double value = metric.getMetricsMap().get(metricService.findMetricIdAbsentGen(counter));
                        if (value != null) {
                            MapPack mapPackOfObj = mapPackMap.get(metric.getObjId());
                            if (mapPackOfObj != null) {
                                mapPackOfObj.getList("time").add(metric.getTimestamp());
                                mapPackOfObj.getList("value").add(value);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        throw new RuntimeException(t);
                    }

                    @Override
                    public void onCompleted() {
                        try {
                            for (int i = 0; i < objIds.size(); i++) {
                                MapPack mapPackOfObj = mapPackMap.get(objIds.get(i));
                                dout.writeByte(TcpFlag.HasNEXT);
                                if (mapPackOfObj != null) {
                                    dout.writePack(mapPackOfObj);
                                    dout.flush();
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    //    @ServiceHandler(RequestCmd.COUNTER_TODAY_TOT)
//    def getRealDateTotal(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val inout = din.readMapPack();
//        val counter = inout.getText("counter");
//        val mode = inout.getText("mode");
//        val objType = inout.getText("objType");
//        if (StringUtil.isEmpty(objType)) {
//            System.out.println("please check.. COUNTER_TODAY_TOT objType is null");
//            return ;
//        }
//        val date = DateUtil.yyyymmdd();
//
//        val period = TimeTypeEnum.FIVE_MIN;
//        val delta = TimeTypeEnum.getTime(period);
//        val timeLength = (TimeTypeEnum.getTime(TimeTypeEnum.DAY) / delta).toInt
//        val values = Array[Double](timeLength);
//        val cnt = Array[Int](timeLength);
//
//        val objPackMap = AgentManager.getCurrentObjects(objType);
//        val objHashLv = objPackMap.getList("objHash");
//        for (i <- 0 to ArrayUtil.len(objHashLv) - 1) {
//            val objHash = objHashLv.getInt(i);
//            try {
//                val ck = new CounterKey(objHash, counter, period);
//                val v = DailyCounterRD.getValues(date, ck);
//                for (j <- 0 to ArrayUtil.len(v) - 1) {
//                    val value = v(j);
//                    var doubleValue = if (value == null) 0.0 else CastUtil.cdouble(value);
//                    if (doubleValue > 0) {
//                        cnt(j) += 1;
//                        values(j) += doubleValue;
//                    }
//                }
//            } catch {
//                case e: Throwable =>
//                    val op = AgentManager.getAgent(objHash);
//                    println(op.objName + " invalid data : " + e.getMessage())
//                    e.printStackTrace()
//            }
//        }
//        val stime = DateUtil.yyyymmdd(date);
//        val mpack = new MapPack();
//        val timeLv = mpack.newList("time");
//        val valueLv = mpack.newList("value");
//        val isAvg = "avg".equalsIgnoreCase(mode)
//
//        for (i <- 0 to values.length - 1) {
//            timeLv.add(stime + (delta * i));
//            if (isAvg && cnt(i) > 1) {
//                values(i) = values(i) / cnt(i);
//            }
//            valueLv.add(new DoubleValue(values(i)));
//        }
//        dout.writeByte(TcpFlag.HasNEXT);
//        dout.writePack(mpack);
//    }
//
//    @ServiceHandler(RequestCmd.COUNTER_PAST_DATE_TOT)
//    def getPastDateTotalCounter(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readMapPack();
//        val counter = param.getText("counter");
//        val date = param.getText("date");
//        val mode = param.getText("mode");
//        val objType = param.getText("objType");
//        if (StringUtil.isEmpty(objType)) {
//            System.out.println("please check.. COUNTER_PAST_DATE_TOT objType is null");
//            return ;
//        }
//
//        val period = TimeTypeEnum.FIVE_MIN;
//        val delta = TimeTypeEnum.getTime(period);
//        val timeLength = (TimeTypeEnum.getTime(TimeTypeEnum.DAY) / delta).toInt
//        val values = new Array[Double](timeLength);
//        val cnt = new Array[Int](timeLength);
//
//        val agentGrp = AgentManager.getDailyObjects(date, objType);
//        val objHashLv = agentGrp.getList("objHash");
//        for (i <- 0 to ArrayUtil.len(objHashLv) - 1) {
//            val objHash = objHashLv.getInt(i);
//            val ckey = new CounterKey(objHash, counter, period);
//            val outvalue = DailyCounterRD.getValues(date, ckey);
//            for (j <- 0 to ArrayUtil.len(outvalue) - 1) {
//                val value = outvalue(j);
//                if (value != null) {
//                    cnt(j) += 1;
//                    values(j) += CastUtil.cdouble(value);
//                }
//            }
//
//        }
//
//        val stime = DateUtil.yyyymmdd(date);
//        val mpack = new MapPack();
//        val timeLv = mpack.newList("time");
//        val valueLv = mpack.newList("value");
//
//        val isAvg = "avg".equalsIgnoreCase(mode);
//
//        for (i <- 0 to values.length - 1) {
//            timeLv.add(stime + (delta * i));
//            if (isAvg && cnt(i) > 1) {
//                values(i) /= cnt(i);
//            }
//            valueLv.add(new DoubleValue(values(i)));
//        }
//        dout.writeByte(TcpFlag.HasNEXT);
//        dout.writePack(mpack);
//    }
//
//    @ServiceHandler(RequestCmd.COUNTER_PAST_DATE_ALL)
//    def getPastDateAll(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readMapPack();
//        val counter = param.getText("counter");
//        val date = param.getText("date");
//        val objType = param.getText("objType");
//        if (StringUtil.isEmpty(objType)) {
//            System.out.println("please check.. COUNTER_LOAD_DATE_ALL objType is null");
//            return ;
//        }
//        val agentGrp = AgentManager.getDailyObjects(date, objType);
//        val stime = DateUtil.yyyymmdd(date);
//        val objHashLv = agentGrp.getList("objHash");
//        for (i <- 0 to ArrayUtil.len(objHashLv) - 1) {
//
//            val objHash = objHashLv.getInt(i);
//
//            val mpack = new MapPack();
//            mpack.put("objHash", objHash);
//            val timeLv = mpack.newList("time");
//            val valueLv = mpack.newList("value");
//
//            val v = DailyCounterRD.getValues(date, new CounterKey(objHash, counter, TimeTypeEnum.FIVE_MIN));
//
//            for (j <- 0 to ArrayUtil.len(v) - 1) {
//                val time = stime + DateUtil.MILLIS_PER_MINUTE * 5 * j;
//                timeLv.add(time);
//                valueLv.add(v(j));
//            }
//
//            dout.writeByte(TcpFlag.HasNEXT);
//            dout.writePack(mpack);
//            dout.flush();
//        }
//    }
//
    @LegacyServiceHandler(RequestCmd.COUNTER_PAST_LONGDATE_ALL)
    public void getPastLongDateAll(DataInputX din, DataOutputX dout, Boolean login) throws IOException {
        MapPack param = din.readMapPack();
        TimeTypeP timeType = TimeTypeP.FIVE_MIN;
        String counter = param.getText("counter");
        String objType = param.getText("objType");
        val objHashParamLv = param.getList("objHash");
        String sDate = param.getText("sDate");
        String eDate = param.getText("eDate");

        if (StringUtils.isBlank(objType) && (objHashParamLv == null || objHashParamLv.size() == 0)) {
            log.warn("please check.. COUNTER_LOAD_TIME_ALL objType is null");
            return ;
        }

        MutableList<Obj> objs = objService.findByLegacyObjType(objType);
        MutableLongList objIds = objs.collectLong(Obj::getObjId);

        long stime = DateUtil.yyyymmdd(sDate);
        long etime = DateUtil.yyyymmdd(eDate) + DateUtil.MILLIS_PER_DAY;

        MutableLongObjectMap<MapPack> mapPackMap = LongObjectMaps.mutable.empty();
        objIds.forEach(objId -> {
            MapPack mapPack = new MapPack();
            mapPack.put("objHash", objId);
            mapPack.newList("time");
            mapPack.newList("value");
            mapPackMap.put(objId, mapPack);
        });

        metricService.streamListByObjs(
                LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT,
                objIds.toSet(),
                stime,
                etime,
                timeType,
                new StreamObserver<Metric4RepoP>() {
                    @Override
                    public void onNext(Metric4RepoP metric) {
                        Double value = metric.getMetricsMap().get(metricService.findMetricIdAbsentGen(counter));
                        if (value != null) {
                            MapPack mapPackOfObj = mapPackMap.get(metric.getObjId());
                            if (mapPackOfObj != null) {
                                mapPackOfObj.getList("time").add(metric.getTimestamp());
                                mapPackOfObj.getList("value").add(value);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        throw new RuntimeException(t);
                    }

                    @Override
                    public void onCompleted() {
                        try {
                            for (int i = 0; i < objIds.size(); i++) {
                                MapPack mapPackOfObj = mapPackMap.get(objIds.get(i));
                                dout.writeByte(TcpFlag.HasNEXT);
                                if (mapPackOfObj != null) {
                                    dout.writePack(mapPackOfObj);
                                    dout.flush();
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

//    @ServiceHandler(RequestCmd.COUNTER_PAST_LONGDATE_TOT)
//    def getPastLongDateTot(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readMapPack();
//        val counter = param.getText("counter");
//        val sDate = param.getText("sDate");
//        val eDate = param.getText("eDate");
//        val mode = param.getText("mode");
//        val objType = param.getText("objType");
//        if (StringUtil.isEmpty(objType)) {
//            System.out.println("please check.. COUNTER_PAST_LONGDATE_TOT objType is null");
//            return ;
//        }
//
//        val stime = DateUtil.yyyymmdd(sDate);
//        val etime = DateUtil.yyyymmdd(eDate) + DateUtil.MILLIS_PER_DAY;
//
//        val period = TimeTypeEnum.FIVE_MIN;
//        val delta = TimeTypeEnum.getTime(period);
//        val timeLength = ((etime - stime) / delta).toInt
//        val values = new Array[Double](timeLength);
//        val cnt = new Array[Int](timeLength);
//
//        val agentGrp = AgentManager.getPeriodicObjects(sDate, eDate, objType);
//
//        var date = stime;
//        var dayPointer = 0;
//        //    val cntPerDay = (TimeTypeEnum.getTime(DAY) / TimeTypeEnum.getTime(FIVE_MIN)).toInt
//        val cntPerDay = 288
//
//        while (date <= (etime - DateUtil.MILLIS_PER_DAY)) {
//            val d = DateUtil.yyyymmdd(date);
//
//            val objHashLv = agentGrp.getList("objHash");
//            for (i <- 0 to ArrayUtil.len(objHashLv) - 1) {
//                val objHash = objHashLv.getInt(i);
//                try {
//                    val ckey = new CounterKey(objHash, counter, period);
//                    val outvalue = DailyCounterRD.getValues(d, ckey);
//                    for (j <- 0 to ArrayUtil.len(outvalue) - 1) {
//                        val value = outvalue(j);
//                        var doubleValue = CastUtil.cdouble(value);
//
//                        if (doubleValue > 0) {
//                            cnt(dayPointer + j) += 1;
//                            values(dayPointer + j) += doubleValue;
//                        }
//                    }
//                } catch {
//                    case e: Throwable =>
//                        val op = AgentManager.getAgent(objHash);
//                        println(op.objName + " invalid data : " + e.getMessage())
//                        e.printStackTrace()
//                }
//            }
//            date += DateUtil.MILLIS_PER_DAY;
//            dayPointer += cntPerDay;
//        }
//
//        val t = DateUtil.yyyymmdd(sDate);
//        val mpack = new MapPack();
//        val timeLv = mpack.newList("time");
//        val valueLv = mpack.newList("value");
//
//        val isAvg = "avg".equalsIgnoreCase(mode);
//
//        for (i <- 0 to ArrayUtil.len(values) - 1) {
//            timeLv.add(t + (delta * i));
//            if (isAvg && cnt(i) > 1) {
//                values(i) /= cnt(i);
//            }
//            val value = new DoubleValue(values(i));
//            valueLv.add(value);
//        }
//        dout.writeByte(TcpFlag.HasNEXT);
//        dout.writePack(mpack);
//    }
//
//    @ServiceHandler(RequestCmd.COUNTER_PAST_DATE)
//    def getPastDate(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readMapPack();
//
//        val date = param.getText("date");
//        val objHash = param.getInt("objHash");
//        val counter = param.getText("counter");
//
//        val stime = DateUtil.yyyymmdd(date);
//
//        val mpack = new MapPack();
//        val timeLv = mpack.newList("time");
//        val valueLv = mpack.newList("value");
//
//        val ckey = new CounterKey(objHash, counter, TimeTypeEnum.FIVE_MIN);
//        val outvalue = DailyCounterRD.getValues(date, ckey)
//        if (outvalue == null)
//            return ;
//
//        for (i <- 0 to ArrayUtil.len(outvalue) - 1) {
//            val time = stime + DateUtil.MILLIS_PER_MINUTE * 5 * i;
//            val value = outvalue(i);
//            timeLv.add(time);
//            valueLv.add(value);
//        }
//
//        dout.writeByte(TcpFlag.HasNEXT);
//        dout.writePack(mpack);
//        dout.flush();
//    }
//
//    @ServiceHandler(RequestCmd.GET_COUNTER_EXIST_DAYS)
//    def getCounterExistDays(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readMapPack();
//        val counter = param.getText("counter");
//        val objType = param.getText("objType");
//        val duration = param.getInt("duration");
//        val etime = param.getLong("etime");
//        val stime = etime - (duration * DateUtil.MILLIS_PER_DAY);
//
//        val p = new MapPack();
//        val dateLv = p.newList("date");
//        val existLv = p.newList("exist");
//        var time = stime;
//
//        for (i <- 0 to duration) {
//            var result = false;
//            val yyyymmdd = DateUtil.yyyymmdd(time);
//            val agentGrp = AgentManager.getDailyObjects(yyyymmdd, objType);
//            val objHashLv = agentGrp.getList("objHash");
//            for (j <- 0 to ArrayUtil.len(objHashLv) - 1) {
//                val objHash = objHashLv.getInt(j);
//                val v = DailyCounterRD.getValues(yyyymmdd, new CounterKey(objHash, counter, TimeTypeEnum.FIVE_MIN));
//                if (ArrayUtil.len(v) > 0) {
//                    result = true;
//                }
//            }
//            dateLv.add(yyyymmdd);
//            existLv.add(result);
//            time += DateUtil.MILLIS_PER_DAY;
//        }
//        dout.writeByte(TcpFlag.HasNEXT);
//        dout.writePack(p);
//    }
//
//    @ServiceHandler(RequestCmd.COUNTER_PAST_DATE_GROUP)
//    def getPastDateGroup(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readMapPack();
//        val counter = param.getText("counter");
//        val date = param.getText("date");
//        val objHashLv = param.getList("objHash");
//        val stime = DateUtil.yyyymmdd(date);
//
//        for (i <- 0 to ArrayUtil.len(objHashLv) - 1) {
//            val objHash = objHashLv.getInt(i);
//            val key = new CounterKey(objHash, counter, TimeTypeEnum.FIVE_MIN);
//            val mpack = new MapPack();
//            mpack.put("objHash", objHash);
//            val timeLv = mpack.newList("time");
//            val valueLv = mpack.newList("value");
//            val v = DailyCounterRD.getValues(date, key);
//            for (j <- 0 to ArrayUtil.len(v) - 1) {
//                val time = stime + DateUtil.MILLIS_PER_MINUTE * 5 * j;
//                val value = v(j);
//                timeLv.add(time);
//                valueLv.add(value);
//            }
//            dout.writeByte(TcpFlag.HasNEXT);
//            dout.writePack(mpack);
//            dout.flush();
//        }
//    }
//
//    @ServiceHandler(RequestCmd.COUNTER_TODAY_GROUP)
//    def getTodayGroup(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readMapPack();
//        val counter = param.getText("counter");
//        val objHashLv = param.getList("objHash");
//
//        val date = DateUtil.yyyymmdd();
//        val stime = DateUtil.yyyymmdd(date);
//
//        for (i <- 0 to ArrayUtil.len(objHashLv) - 1) {
//            val objHash = objHashLv.getInt(i);
//
//            try {
//                val ck = new CounterKey(objHash, counter, TimeTypeEnum.FIVE_MIN);
//                val v = DailyCounterRD.getValues(date, ck);
//
//                val mpack = new MapPack();
//                mpack.put("objHash", objHash);
//                val timeLv = mpack.newList("time");
//                val valueLv = mpack.newList("value");
//
//                val delta = TimeTypeEnum.getTime(ck.timetype);
//                for (j <- 0 to ArrayUtil.len(v) - 1) {
//                    val time = stime + delta * j;
//                    val value = if (v(j) != null) v(j) else new NullValue()
//                    timeLv.add(time);
//                    valueLv.add(value);
//                }
//                dout.writeByte(TcpFlag.HasNEXT);
//                dout.writePack(mpack);
//            } catch {
//                case e: Throwable =>
//                    val op = AgentManager.getAgent(objHash);
//                    println(op.objName + " invalid data : " + e.getMessage())
//                    e.printStackTrace()
//            }
//        }
//    }
//
//    @ServiceHandler(RequestCmd.COUNTER_PAST_LONGDATE_GROUP)
//    def getPastLongDateGroup(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readMapPack();
//        val counter = param.getText("counter");
//        val stime = param.getLong("stime");
//        val etime = param.getLong("etime");
//        val lastDay = DateUtil.yyyymmdd(etime);
//        val objHashLv = param.getList("objHash");
//        for (i <- 0 to ArrayUtil.len(objHashLv) - 1) {
//            val objHash = objHashLv.getInt(i);
//            try {
//                var time = stime;
//                val key = new CounterKey(objHash, counter, TimeTypeEnum.FIVE_MIN);
//                val mpack = new MapPack();
//                mpack.put("objHash", objHash);
//                val timeLv = mpack.newList("time");
//                val valueLv = mpack.newList("value");
//                var date: String = null;
//                while (lastDay.equals(date) == false) {
//                    date = DateUtil.yyyymmdd(time);
//                    val oclock = DateUtil.yyyymmdd(date);
//                    val v = DailyCounterRD.getValues(date, key);
//                    if (v == null) {
//                        for (j <- 0 to 287) {
//                            timeLv.add(oclock + DateUtil.MILLIS_PER_FIVE_MINUTE * j);
//                            valueLv.add(new NullValue());
//                        }
//                    } else {
//                        for (j <- 0 to v.length - 1) {
//                            timeLv.add(oclock + DateUtil.MILLIS_PER_FIVE_MINUTE * j);
//                            valueLv.add(v(j));
//                        }
//                    }
//                    time += DateUtil.MILLIS_PER_DAY;
//                }
//                dout.writeByte(TcpFlag.HasNEXT);
//                dout.writePack(mpack);
//                dout.flush();
//            } catch {
//                case e: Throwable =>
//                    val op = AgentManager.getAgent(objHash);
//                    println(op.objName + " invalid data : " + e.getMessage())
//                    e.printStackTrace()
//            }
//        }
//    }
}
