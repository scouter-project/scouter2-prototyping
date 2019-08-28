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
import scouter.net.RequestCmd;
import scouter2.collector.transport.legacy.service.annotation.LegacyServiceHandler;

import java.io.IOException;

public class LegacyManageServiceHandler {

	//TODO modify objType and ask to all client to refetch
//	@LegacyServiceHandler(RequestCmd.REMOTE_CONTROL_ALL)
//	public void remoteControlAll(DataInputX din, DataOutputX dout, boolean login) throws IOException {
//		MapPack param = (MapPack) din.readPack();
//		RemoteControl control = new RemoteControl(//
//				param.getText("command"), //
//				System.currentTimeMillis(), //
//				param, param.getLong("fromSession"));
//		LoginUser[] users = LoginManager.getLoginUserList();
//		for (int i = 0, len = (users != null ? users.length : 0); i < len; i++) {
//			long session = users[i].session();
//			RemoteControlManager.add(session, control);
//		}
//		Logger.println("[" + RequestCmd.REMOTE_CONTROL_ALL + "]" + control.commnad() + " from "
//				+ LoginManager.getUser(control.commander()).ip());
//	}

	@LegacyServiceHandler(RequestCmd.CHECK_JOB)
	public void checkJob(DataInputX din, DataOutputX dout, boolean login) throws IOException {
		MapPack param = (MapPack) din.readPack();
		long session = param.getLong("session");
        //TODO check job todo
//		RemoteControl control = RemoteControlManager.getCommand(session);
//		if (control != null) {
//			TextPack t = new TextPack();
//			t.text = control.commnad();
//			dout.writeByte(TcpFlag.HasNEXT);
//			dout.writePack(t);
//			dout.writeByte(TcpFlag.HasNEXT);
//			dout.writePack(control.param());
//		}
	}
}
