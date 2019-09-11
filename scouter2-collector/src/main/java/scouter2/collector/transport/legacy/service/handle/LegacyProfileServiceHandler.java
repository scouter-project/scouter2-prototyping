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
import scouter.lang.pack.XLogProfilePack;
import scouter.net.RequestCmd;
import scouter.net.TcpFlag;
import scouter.util.StringUtil;
import scouter2.collector.config.ConfigXlog;
import scouter2.collector.config.support.ConfigManager;
import scouter2.collector.domain.profile.ProfileService;
import scouter2.collector.springconfig.ApplicationContextHolder;
import scouter2.collector.transport.legacy.service.annotation.LegacyServiceHandler;
import scouter2.common.support.XlogIdSupport;
import scouter2.common.util.DateUtil;

import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 30/08/2019
 */
public class LegacyProfileServiceHandler {

    private ConfigXlog configXlog = ConfigManager.getConfig(ConfigXlog.class);
    ProfileService profileService = ApplicationContextHolder.getBean(ProfileService.class);

    @LegacyServiceHandler(RequestCmd.TRANX_PROFILE)
    public void getProfile(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = din.readMapPack();

        String date = param.getText("date");
        long txid = param.getLong("txid");
        long gxid = param.getLong("gxid");
        int xlogType = param.getInt("xlogType");
        int max = param.getInt("max");
        if (StringUtil.isEmpty(date)) {
            date = DateUtil.yyyymmdd();
        }

        //TODO Zipkin spans
        byte[] stepsBytes = profileService.findStepsAsLegacyType(XlogIdSupport.createId(date, txid), max);
        if (stepsBytes.length > 0) {
            dout.writeByte(TcpFlag.HasNEXT);
            XLogProfilePack pack = new XLogProfilePack();
            pack.profile = stepsBytes;
            dout.writePack(pack);
        }
    }

    @LegacyServiceHandler(RequestCmd.TRANX_PROFILE_FULL)
    public void getProfileFull(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        MapPack param = din.readMapPack();

        String date = param.getText("date");
        long txid = param.getLong("txid");
        long gxid = param.getLong("gxid");
        int xlogType = param.getInt("xlogType");
        if (StringUtil.isEmpty(date)) {
            date = DateUtil.yyyymmdd();
        }

        //TODO Zipkin spans
        byte[] stepsBytes = profileService.findStepsAsLegacyType(XlogIdSupport.createId(date, txid), Integer.MAX_VALUE);
        if (stepsBytes.length > 0) {
            dout.writeByte(TcpFlag.HasNEXT);
            dout.writeBlob(stepsBytes);
        }
    }
}
