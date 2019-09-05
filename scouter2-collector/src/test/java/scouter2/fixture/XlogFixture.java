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

package scouter2.fixture;

import com.google.protobuf.ByteString;
import scouter2.collector.common.util.U;
import scouter2.collector.domain.xlog.Xlog;
import scouter2.proto.XlogP;
import scouter2.proto.XlogTypeP;
import scouter2.testsupport.T;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-07
 */
public class XlogFixture {

    public static Xlog getOne() {
        byte[] ip = {127, 0, 0, 1};
        XlogP.Builder builder = XlogP.newBuilder()
                .setGxid(T.xlogIdAsBs())
                .setTxid(T.xlogIdAsBs())
                .setPtxid(T.xlogIdAsBs())
                .setObjId(100)
                .setLegacyObjHash(1004)
                .setXlogType(XlogTypeP.WEB_SERVICE)
                .setService(1006)
                .setEndTime(U.now())
                .setThreadName(1007)
                .setError(1008)
                .setCpuTime(3)
                .setSqlCount(2)
                .setIpaddr(ByteString.copyFrom(ip))
                .setMemoryKb(2048)
                .setUserId(101)
                .setUserAgent(102)
                .setReferrer(103)
                .setApiCallCount(3)
                .setApiCallTime(102)
                .setGroup(113)
                .setCountryCode("KR")
                .setCity(115)
                .setHasDump(true)
                .setB3Mode(false)
                .setProfileCount(251);

        builder.putDictTags("login", 1);
        builder.putDictTags("desc", 2);
        builder.putStringTags("text1", "t1");
        builder.putStringTags("text2", "t2");

        return new Xlog(builder.build(), "testapp");
    }

    public static Xlog getOne(long endTime, long objId) {
        byte[] ip = {127, 0, 0, 1};
        XlogP.Builder builder = XlogP.newBuilder()
                .setGxid(T.xlogIdAsBs(endTime))
                .setTxid(T.xlogIdAsBs(endTime))
                .setPtxid(T.xlogIdAsBs(endTime))
                .setObjId(objId)
                .setLegacyObjHash(1004)
                .setXlogType(XlogTypeP.WEB_SERVICE)
                .setService(1006)
                .setEndTime(endTime)
                .setThreadName(1007)
                .setError(1008)
                .setCpuTime(3)
                .setSqlCount(2)
                .setIpaddr(ByteString.copyFrom(ip))
                .setMemoryKb(2048)
                .setUserId(101)
                .setUserAgent(102)
                .setReferrer(103)
                .setApiCallCount(3)
                .setApiCallTime(102)
                .setGroup(113)
                .setCountryCode("KR")
                .setCity(115)
                .setHasDump(true)
                .setB3Mode(false)
                .setProfileCount(251);

        builder.putDictTags("login", 1);
        builder.putDictTags("desc", 2);
        builder.putStringTags("text1", "t1");
        builder.putStringTags("text2", "t2");

        return new Xlog(builder.build(), endTime, "testapp");
    }

    public static Xlog getOne(long endTime, String applicationId, long objId) {
        byte[] ip = {127, 0, 0, 1};
        XlogP.Builder builder = XlogP.newBuilder()
                .setGxid(T.xlogIdAsBs(endTime))
                .setTxid(T.xlogIdAsBs(endTime))
                .setPtxid(T.xlogIdAsBs(endTime))
                .setObjId(objId)
                .setLegacyObjHash(1004)
                .setXlogType(XlogTypeP.WEB_SERVICE)
                .setService(1006)
                .setEndTime(endTime)
                .setThreadName(1007)
                .setError(1008)
                .setCpuTime(3)
                .setSqlCount(2)
                .setIpaddr(ByteString.copyFrom(ip))
                .setMemoryKb(2048)
                .setUserId(101)
                .setUserAgent(102)
                .setReferrer(103)
                .setApiCallCount(3)
                .setApiCallTime(102)
                .setGroup(113)
                .setCountryCode("KR")
                .setCity(115)
                .setHasDump(true)
                .setB3Mode(false)
                .setProfileCount(251);

        builder.putDictTags("login", 1);
        builder.putDictTags("desc", 2);
        builder.putStringTags("text1", "t1");
        builder.putStringTags("text2", "t2");

        return new Xlog(builder.build(), endTime, applicationId);
    }

    public static Xlog getOneOfGxid(byte[] gxid, long endTime, String applicationId, long objId) {
        byte[] ip = {127, 0, 0, 1};
        XlogP.Builder builder = XlogP.newBuilder()
                .setGxid(ByteString.copyFrom(gxid))
                .setTxid(T.xlogIdAsBs(endTime))
                .setPtxid(T.xlogIdAsBs(endTime))
                .setObjId(objId)
                .setLegacyObjHash(1004)
                .setXlogType(XlogTypeP.WEB_SERVICE)
                .setService(1006)
                .setEndTime(endTime)
                .setThreadName(1007)
                .setError(1008)
                .setCpuTime(3)
                .setSqlCount(2)
                .setIpaddr(ByteString.copyFrom(ip))
                .setMemoryKb(2048)
                .setUserId(101)
                .setUserAgent(102)
                .setReferrer(103)
                .setApiCallCount(3)
                .setApiCallTime(102)
                .setGroup(113)
                .setCountryCode("KR")
                .setCity(115)
                .setHasDump(true)
                .setB3Mode(false)
                .setProfileCount(251);

        builder.putDictTags("login", 1);
        builder.putDictTags("desc", 2);
        builder.putStringTags("text1", "t1");
        builder.putStringTags("text2", "t2");

        return new Xlog(builder.build(), endTime, applicationId);
    }

    public static Xlog getOneOfGxid(byte[] gxid, byte[] txid, long endTime, String applicationId, long objId) {
        byte[] ip = {127, 0, 0, 1};
        XlogP.Builder builder = XlogP.newBuilder()
                .setGxid(ByteString.copyFrom(gxid))
                .setTxid(ByteString.copyFrom(txid))
                .setPtxid(T.xlogIdAsBs(endTime))
                .setObjId(objId)
                .setLegacyObjHash(1004)
                .setXlogType(XlogTypeP.WEB_SERVICE)
                .setService(1006)
                .setEndTime(endTime)
                .setThreadName(1007)
                .setError(1008)
                .setCpuTime(3)
                .setSqlCount(2)
                .setIpaddr(ByteString.copyFrom(ip))
                .setMemoryKb(2048)
                .setUserId(101)
                .setUserAgent(102)
                .setReferrer(103)
                .setApiCallCount(3)
                .setApiCallTime(102)
                .setGroup(113)
                .setCountryCode("KR")
                .setCity(115)
                .setHasDump(true)
                .setB3Mode(false)
                .setProfileCount(251);

        builder.putDictTags("login", 1);
        builder.putDictTags("desc", 2);
        builder.putStringTags("text1", "t1");
        builder.putStringTags("text2", "t2");

        return new Xlog(builder.build(), endTime, applicationId);
    }
}