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
import scouter.lang.value.ListValue;
import scouter.net.RequestCmd;
import scouter.net.TcpFlag;
import scouter.util.Hexa32;
import scouter2.collector.domain.dict.DictCategory;
import scouter2.collector.domain.dict.DictService;
import scouter2.collector.legacy.LegacySupport;
import scouter2.collector.springconfig.ApplicationContextHolder;
import scouter2.collector.transport.legacy.service.annotation.LegacyServiceHandler;

import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 31/08/2019
 */
public class LegacyHashTextServiceHandler {

    DictService dictService = ApplicationContextHolder.getBean(DictService.class);

    @LegacyServiceHandler(RequestCmd.GET_TEXT)
    public void getText(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = din.readMapPack();
        DictCategory category = DictCategory.ofLegacy(param.getText("type"));
        ListValue hashLv = param.getList("hash");
        if (hashLv == null) {
            return;
        }

        MapPack result = new MapPack();
        for (int i = 0; i < hashLv.size(); i++) {
            int hash = hashLv.getInt(i);
            String text = dictService.find(LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT,
                    category.getCategory(), hash);
            if (text != null) {
                result.put(Hexa32.toString32(hash), text);
            }
        }
        dout.writeByte(TcpFlag.HasNEXT);
        dout.writePack(result);
    }

    @LegacyServiceHandler(RequestCmd.GET_TEXT_100)
    public void getText100(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = din.readMapPack();
        DictCategory category = DictCategory.ofLegacy(param.getText("type"));
        ListValue hashLv = param.getList("hash");
        if (hashLv == null) {
            return;
        }

        MapPack result = new MapPack();
        for (int i = 0; i < hashLv.size(); i++) {
            int hash = hashLv.getInt(i);
            String text = dictService.find(LegacySupport.APPLICATION_ID_FOR_SCOUTER1_AGENT,
                    category.getCategory(), hash);
            if (text != null) {
                result.put(Hexa32.toString32(hash), text);
                if (result.size() == 100) {
                    dout.writeByte(TcpFlag.HasNEXT);
                    dout.writePack(result);
                    result.clear();
                }
            }
        }
        if (result.size() > 0) {
            dout.writeByte(TcpFlag.HasNEXT);
            dout.writePack(result);
        }
    }

//    @ServiceHandler(RequestCmd.GET_TEXT_PACK)
//    def getTextPack(din: DataInputX, dout: DataOutputX, login: Boolean) {
//        val param = din.readMapPack()
//        var date = param.getText("date")
//        val xtype = param.getText("type")
//        val hash = param.getList("hash")
//        if (hash == null)
//            return
//
//        if (date == null)
//            date = DateUtil.yyyymmdd()
//
//        for (i <- 0 to ArrayUtil.len(hash) - 1) {
//            val h = hash.getInt(i);
//            val v = TextRD.getString(date, xtype, h)
//            if (v != null) {
//                dout.writeByte(TcpFlag.HasNEXT);
//                dout.writePack(new TextPack(xtype, h, v))
//            }
//        }
//        dout.flush()
//    }
//
//    /**
//      *
//      * @param din MapPack{date: String, type: ListValue<String>, hash: ListValue<int>}
//      * @param dout TextPack[]
//      */
//    @ServiceHandler(RequestCmd.GET_TEXT_ANY_TYPE)
//    def getTextAnyType(din:DataInputX, dout:DataOutputX, login: Boolean) {
//        val param = din.readMapPack()
//        var date = param.getText("date")
//        val typeList = param.getList("type")
//        val hashList = param.getList("hash")
//
//        if (typeList == null || hashList == null) {
//            return
//        }
//
//        if (date == null) {
//            date = DateUtil.yyyymmdd()
//        }
//
//        for (i <- 0 to ArrayUtil.len(hashList) - 1) {
//            val _type = typeList.getString(i);
//            val _hash = hashList.getInt(i);
//
//            val value = TextRD.getString(date, _type, _hash);
//            if (value != null) {
//                dout.writeByte(TcpFlag.HasNEXT)
//                TextPack.writeDirect(dout, PackEnum.TEXT, _type, _hash, value)
//            }
//
//        }
//        dout.flush()
//    }
}
