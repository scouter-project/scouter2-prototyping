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
import scouter.lang.value.BooleanValue;
import scouter.lang.value.MapValue;
import scouter.net.RequestCmd;
import scouter2.collector.transport.legacy.service.annotation.LegacyServiceHandler;

import java.io.IOException;
import java.util.TimeZone;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-11
 */
public class LegacyLoginServiceHandler {

    /*
    <Groups>
	<Group name="admin">
		<Policy>
			<AllowEditGroupPolicy>true</AllowEditGroupPolicy>
			<AllowHeapDump>true</AllowHeapDump>
			<AllowFileDump>true</AllowFileDump>
			<AllowHeapHistogram>true</AllowHeapHistogram>
			<AllowThreadDump>true</AllowThreadDump>
			<AllowSystemGC>true</AllowSystemGC>
			<AllowConfigure>true</AllowConfigure>
			<AllowExportCounter>true</AllowExportCounter>
			<AllowExportAppSum>true</AllowExportAppSum>
			<AllowLoginList>true</AllowLoginList>
			<AllowDBManager>true</AllowDBManager>
			<AllowAddAccount>true</AllowAddAccount>
			<AllowEditAccount>true</AllowEditAccount>
			<AllowSqlParameter>true</AllowSqlParameter>
			<AllowKillTransaction>true</AllowKillTransaction>
			<AllowExportClass>true</AllowExportClass>
			<AllowRedefineClass>true</AllowRedefineClass>
			<AllowDefineObjectType>true</AllowDefineObjectType>
		</Policy>
	</Group>

	<Group name="guest">
		<Policy>
			<AllowEditGroupPolicy>false</AllowEditGroupPolicy>
			<AllowHeapDump>false</AllowHeapDump>
			<AllowFileDump>false</AllowFileDump>
			<AllowHeapHistogram>false</AllowHeapHistogram>
			<AllowThreadDump>true</AllowThreadDump>
			<AllowSystemGC>true</AllowSystemGC>
			<AllowConfigure>false</AllowConfigure>
			<AllowExportCounter>false</AllowExportCounter>
			<AllowExportAppSum>false</AllowExportAppSum>
			<AllowLoginList>false</AllowLoginList>
			<AllowDBManager>false</AllowDBManager>
			<AllowAddAccount>false</AllowAddAccount>
			<AllowEditAccount>false</AllowEditAccount>
			<AllowSqlParameter>false</AllowSqlParameter>
			<AllowKillTransaction>false</AllowKillTransaction>
			<AllowExportClass>false</AllowExportClass>
			<AllowRedefineClass>false</AllowRedefineClass>
			<AllowDefineObjectType>false</AllowDefineObjectType>
		</Policy>
	</Group>
</Groups>
     */
    @LegacyServiceHandler(RequestCmd.LOGIN)
    public void login(DataInputX din, DataOutputX dout, boolean login) throws IOException {
        //Temp logic for skip login
        MapPack m = din.readMapPack();
        String id = m.getText("id");
        String passwd = m.getText("pass");
        String ip = m.getText("ip");
        String name = m.getText("hostname");
        String clientVer = m.getText("version");
        String internal = m.getText("internal");
        boolean internalMode = (internal != null) && (internal.equalsIgnoreCase("true"));

        //TODO Policy
        MapValue mv = new MapValue();

        mv.put("AllowEditGroupPolicy", new BooleanValue(true));
        mv.put("AllowHeapDump", new BooleanValue(true));
        mv.put("AllowFileDump", new BooleanValue(true));
        mv.put("AllowHeapHistogram", new BooleanValue(true));
        mv.put("AllowThreadDump", new BooleanValue(true));
        mv.put("AllowSystemGC", new BooleanValue(true));
        mv.put("AllowConfigure", new BooleanValue(true));
        mv.put("AllowExportCounter", new BooleanValue(true));
        mv.put("AllowExportAppSum", new BooleanValue(true));
        mv.put("AllowLoginList", new BooleanValue(true));
        mv.put("AllowDBManager", new BooleanValue(true));
        mv.put("AllowAddAccount", new BooleanValue(true));
        mv.put("AllowEditAccount", new BooleanValue(true));
        mv.put("AllowSqlParameter", new BooleanValue(true));
        mv.put("AllowKillTransaction", new BooleanValue(true));
        mv.put("AllowExportClass", new BooleanValue(true));
        mv.put("AllowRedefineClass", new BooleanValue(true));
        mv.put("AllowDefineObjectType", new BooleanValue(true));

        m.put("policy", mv);

        //long session = LoginManager.login(id, passwd, ip, internalMode);
        long session = 1;

        m.put("session", session);

        m.put("time", System.currentTimeMillis());
        m.put("server_id", 100);
        m.put("type", "admin");
        m.put("version", Version.getServerFullVersion());
        m.put("client_version", Version.getServerRecommendedClientVersion());

        m.put("timezone", TimeZone.getDefault().getDisplayName());
        MapValue menuMv = new MapValue();
        m.put("menu", menuMv);
        m.put("so_time_out", 60000);
        m.put("ext_link_name", "extlinkname");

        dout.writeByte(3);
        dout.writePack(m);
    }

//    @LegacyServiceHandler("LOGIN")
//    public void login(DataInputX din, DataOutputX dout, boolean login) {
//        MapPack m = din.readMapPack();
//        String id = m.getText("id");
//        String passwd = m.getText("pass");
//        String ip = m.getText("ip");
//        String name = m.getText("hostname");
//        String clientVer = m.getText("version");
//        String internal = m.getText("internal");
//        boolean internalMode = (internal != null) && (internal.equalsIgnoreCase("true"));
//
//        long session = LoginManager.login(id, passwd, ip, internalMode);
//
//        m.put("session", session);
//
//        LoginUser user = LoginManager.getUser(session);
//        user.hostname_$eq(name);
//        user.version_$eq(clientVer);
//        m.put("time", System.currentTimeMillis());
//        m.put("server_id", getServerId());
//        m.put("type", user.group());
//        m.put("version", Version.getServerFullVersion());
//        m.put("client_version", Version.getServerRecommendedClientVersion());
//
//        Account acc = AccountManager..getAccount(id);
//        (acc == null ? BoxedUnit.UNIT :
//                m.put("email", acc.email));
//
//        m.put("timezone", TimeZone.getDefault().getDisplayName());
//        MapValue mv = AccountManager.getGroupPolicy(user.group());
//        (mv == null ? BoxedUnit.UNIT :
//                m.put("policy", mv));
//
//        MapValue menuMv = new MapValue();
//        m.put("menu", menuMv);
//        menuMv.put("tag_count", new BooleanValue(Configure.getInstance().tagcnt_enabled));
//        m.put("so_time_out", Configure.getInstance().net_tcp_client_so_timeout_ms);
//        m.put("ext_link_name", Configure.getInstance().ext_link_name);(session == 0L ? m.put("error", "login fail") :
//            m.put("ext_link_url_pattern", Configure.getInstance().ext_link_url_pattern));
//
//        dout.writeByte(3);
//        dout.writePack(m);
//    }
//
//    @LegacyServiceHandler("GET_LOGIN_LIST")
//    public void getLoginList(DataInputX din, DataOutputX dout, boolean login)
//    {
//        LoginUser[] users = LoginManager.getLoginUserList();
//        if (ArrayUtil.len(users) > 0)
//        {
//            MapPack result = new MapPack();
//            final ListValue sessionLv = result.newList("session");
//            final ListValue userLv = result.newList("user");
//            final ListValue ipLv = result.newList("ip");
//            final ListValue loginTimeLv = result.newList("logintime");
//            final ListValue versioneLv = result.newList("ver");
//            final ListValue hostnameLv = result.newList("host");
//            Predef.refArrayOps((Object[])users).foreach(new AbstractFunction1()
//        {
//            public static final long serialVersionUID = 0L;
//
//            public final void apply(LoginUser usr)
//            {
//                sessionLv.add(usr.session());
//                userLv.add(usr.id());
//                ipLv.add(usr.ip());
//                loginTimeLv.add((System.currentTimeMillis() - usr.logintime()) / 1000L);
//                versioneLv.add(usr.version());
//                hostnameLv.add(usr.hostname());
//            }
//        });
//            dout.writeByte(3);
//            dout.writePack(result);
//        }
//    }
//
//    @LegacyServiceHandler("CHECK_SESSION")
//    public void getSessionCheck(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack m = din.readMapPack();
//        long session = m.getLong("session");
//        int validSession = LoginManager.validSession(session);
//        m.put("validSession", validSession);
//
//        dout.writeByte(3);
//        dout.writePack(m);
//    }
//
//    public String getServerId()
//    {
//        return Configure.getInstance().server_id;
//    }
//
//    @LegacyServiceHandler("CHECK_LOGIN")
//    public void checkLogin(DataInputX din, DataOutputX dout, boolean login)
//    {
//        MapPack mapPack = din.readMapPack();
//        String id = mapPack.getText("id");
//        String password = mapPack.getText("pass");
//
//        Account account = AccountManager..MODULE$.authorizeAccount(id, password);
//        BooleanValue booleanValue =
//                account == null ? new BooleanValue(false) :
//                        new BooleanValue(true);
//
//        dout.writeByte(3);
//        dout.writeValue(booleanValue);
//    }
}
