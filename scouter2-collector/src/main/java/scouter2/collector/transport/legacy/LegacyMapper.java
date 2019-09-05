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

import com.google.protobuf.ByteString;
import scouter.lang.pack.ObjectPack;
import scouter.lang.pack.PerfCounterPack;
import scouter.lang.pack.TextPack;
import scouter.lang.pack.XLogPack;
import scouter.lang.value.ListValue;
import scouter.lang.value.Value;
import scouter.util.HashUtil;
import scouter2.collector.domain.dict.DictCategory;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.legacy.LegacySupport;
import scouter2.common.legacy.counters.CounterConstants;
import scouter2.common.support.XlogIdSupport;
import scouter2.proto.DictP;
import scouter2.proto.MetricP;
import scouter2.proto.ObjP;
import scouter2.proto.TimeTypeP;
import scouter2.proto.XlogP;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-28
 */
public class LegacyMapper {

    public static ObjP toObjP(ObjectPack objectPack, String legacyFamily) {
        Map<String, String> tagMap = new HashMap<>();
        for (Map.Entry<String, Value> e : objectPack.tags.toMap().entrySet()) {
            tagMap.put(e.getKey(), e.getValue() == null ? "" : e.getValue().toString());
        }

        return ObjP.newBuilder()
                .setApplicationId(LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT)
                .setObjFamily(legacyFamily)
                .setObjLegacyType(objectPack.objType)
                .setObjFullName(objectPack.objName)
                .setLegacyObjHash(objectPack.objHash)
                .setAddress(objectPack.address)
                .setVersion(objectPack.version)
                .putAllTags(tagMap)
                .build();
    }

    public static ObjectPack toObjectPack(Obj obj) {
        ObjectPack pack = new ObjectPack();
        pack.objType = obj.getObjLegacyType();
        pack.objName = obj.getObjFullName();
        pack.objHash = (int) obj.getObjId();
        pack.address = obj.getAddress();
        pack.version = obj.getVersion();

        return pack;
    }

    public static DictP toDict(TextPack p) {
        return DictP.newBuilder()
                .setObjFullName(LegacySupport.DUMMY_OBJ_FULL_NAME_FOR_SCOUTER1_AGENT)
                .setCategory(DictCategory.ofLegacy(p.xtype).getCategory())
                .setDictHash(p.hash)
                .setText(p.text)
                .build();
    }

    public static MetricP toMetric(PerfCounterPack counterPack) {
        MetricP.Builder builder = MetricP.newBuilder()
                .setTimestamp(counterPack.time)
                .setObjFullName(String.valueOf(HashUtil.hash(counterPack.objName)))
                .setLegacyObjHash(HashUtil.hash(counterPack.objName))
                .setTimeType(toTimeTypeP(counterPack.timetype));

        counterPack.data.toMap().forEach((key, value) -> builder.putMetrics(key, doubleValueOf(value)));

        ListValue heapTotUsage = counterPack.data.getList(CounterConstants.JAVA_HEAP_TOT_USAGE);
        ListValue activeSpeed = counterPack.data.getList(CounterConstants.WAS_ACTIVE_SPEED);
        if (heapTotUsage != null) {
            builder.putMetrics(CounterConstants.JAVA_HEAP_TOT_USAGE_TOT, heapTotUsage.getFloat(0));
            builder.putMetrics(CounterConstants.JAVA_HEAP_TOT_USAGE_USAGE, heapTotUsage.getFloat(1));
        }
        if (activeSpeed != null) {
            builder.putMetrics(CounterConstants.WAS_ACTIVE_SPEED_STEP_LOW, activeSpeed.getInt(0));
            builder.putMetrics(CounterConstants.WAS_ACTIVE_SPEED_STEP_MID, activeSpeed.getInt(1));
            builder.putMetrics(CounterConstants.WAS_ACTIVE_SPEED_STEP_HIGH, activeSpeed.getInt(2));
        }

        return builder.build();
    }

    public static XlogP toXlog(XLogPack p, Long objId) {

        XlogP.Builder builder = XlogP.newBuilder()
                .setGxid(ByteString.copyFrom(XlogIdSupport.createId(p.endTime, p.gxid)))
                .setTxid(ByteString.copyFrom(XlogIdSupport.createId(p.endTime, p.txid)))
                .setPtxid(ByteString.copyFrom(XlogIdSupport.createId(p.endTime, p.caller)))
                .setObjId(objId)
                .setLegacyObjHash(p.objHash)
                .setXlogTypeValue(p.xType)
                .setService(p.service)
                .setEndTime(p.endTime)
                .setElapsed(p.elapsed)
                .setThreadName(p.threadNameHash)
                .setError(p.error)
                .setCpuTime(p.cpu)
                .setSqlCount(p.sqlCount)
                .setIpaddr(ByteString.copyFrom(p.ipaddr))
                .setMemoryKb(p.kbytes)
                .setUserId(p.userid)
                .setUserAgent(p.userAgent)
                .setReferrer(p.referer)
                .setApiCallCount(p.apicallCount)
                .setApiCallTime(p.apicallTime)
                .setGroup(p.group)
                .setCountryCode(p.countryCode)
                .setCity(p.city)
                .setQueuingHostHash(p.queuingHostHash)
                .setQueuingTime(p.queuingTime)
                .setQueuing2NdHostHash(p.queuing2ndHostHash)
                .setQueuing2NdTime(p.queuing2ndTime)
                .setHasDump(p.hasDump != 0)
                .setB3Mode(p.b3Mode)
                .setProfileCount(p.profileCount);

        if (p.login != 0) builder.putDictTags("login", p.login);
        if (p.desc != 0) builder.putDictTags("desc", p.desc);

        if (p.text1 != null && p.text1.length() > 0) builder.putStringTags("text1", p.text1);
        if (p.text2 != null && p.text2.length() > 1) builder.putStringTags("text2", p.text2);
        if (p.text3 != null && p.text3.length() > 2) builder.putStringTags("text3", p.text3);
        if (p.text4 != null && p.text4.length() > 3) builder.putStringTags("text4", p.text4);
        if (p.text5 != null && p.text5.length() > 4) builder.putStringTags("text5", p.text5);

        return builder.build();
    }

    public static XLogPack toXlogPack(XlogP xlog) {
        XLogPack p = new XLogPack();
        p.gxid = XlogIdSupport.least(xlog.getGxid().toByteArray());
        p.txid = XlogIdSupport.least(xlog.getTxid().toByteArray());
        p.caller = XlogIdSupport.least(xlog.getPtxid().toByteArray());
        p.objHash = (int) xlog.getObjId();
        p.xType = (byte) xlog.getXlogTypeValue();
        p.service = xlog.getService();
        p.endTime = xlog.getEndTime();
        p.elapsed = xlog.getElapsed();
        p.threadNameHash = xlog.getThreadName();
        p.error = xlog.getError();
        p.cpu = xlog.getCpuTime();
        p.sqlCount = xlog.getSqlCount();
        p.ipaddr = xlog.getIpaddr().toByteArray();
        p.kbytes = xlog.getMemoryKb();
        p.userid = xlog.getUserId();
        p.userAgent = xlog.getUserAgent();
        p.referer = xlog.getReferrer();
        p.apicallCount = xlog.getApiCallCount();
        p.apicallTime = xlog.getApiCallTime();
        p.group = xlog.getGroup();
        p.countryCode = xlog.getCountryCode();
        p.city = xlog.getCity();
        p.queuingHostHash = xlog.getQueuingHostHash();
        p.queuingTime = xlog.getQueuingTime();
        p.queuing2ndHostHash = xlog.getQueuing2NdHostHash();
        p.queuing2ndTime = xlog.getQueuing2NdTime();
        p.hasDump = !xlog.getHasDump() ? (byte) 0 : (byte) 1;
        p.b3Mode = xlog.getB3Mode();
        p.profileCount = xlog.getProfileCount();

        return p;
    }

    private static double doubleValueOf(Value value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0d;
    }

    public static TimeTypeP toTimeTypeP(byte legacyTimeType) {
        if (legacyTimeType == 1) {
            return TimeTypeP.REALTIME;
        }
        if (legacyTimeType == 2) {
            return TimeTypeP.ONE_MIN;
        }
        if (legacyTimeType == 3) {
            return TimeTypeP.FIVE_MIN;
        }
        if (legacyTimeType == 4) {
            return TimeTypeP.TEN_MIN;
        }
        if (legacyTimeType == 5) {
            return TimeTypeP.HOUR;
        }
        if (legacyTimeType == 6) {
            return TimeTypeP.DAY;
        }
        return TimeTypeP.REALTIME;
    }
}
