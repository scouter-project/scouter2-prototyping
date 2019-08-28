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
package scouter2.collector.main;

import com.hazelcast.config.AdvancedNetworkConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.ServerSocketEndpointConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.obj.ObjRepo;
import scouter2.collector.springconfig.InitConfig;
import scouter2.common.util.SysJMX;
import scouter2.common.util.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Component
@Slf4j
public class CollectorMain {

    ObjRepo objRepo;
    ServerBeanInitializer serverBeanInitializer;

    public CollectorMain(InitConfig.Initializer initializer,
                         ServerBeanInitializer serverBeanInitializer,
                         ObjRepo objRepo) {

        initializer.init();
        this.serverBeanInitializer = serverBeanInitializer;
        this.objRepo = objRepo;
    }

    public void start(String[] args) throws IOException {
        //TODO logo
        System.out.println("System JRE version : " + System.getProperty("java.version"));
        testHazelcast();
        //serverBeanInitializer.init();
        startServer();
    }

    private void testHazelcast() {
        if (!(objRepo instanceof NonThreadSafeRepo)) {
            new Thread(() -> {
                Config config = new Config();
                JoinConfig joinConfig = new JoinConfig();
                joinConfig.getMulticastConfig().setEnabled(false);
                joinConfig.getTcpIpConfig().setEnabled(true);
                //.addMember("127.0.0.1");

                ServerSocketEndpointConfig endpointConfig = new ServerSocketEndpointConfig()
                        .setPort(5701)
                        .setPortAutoIncrement(false)
                        .setReuseAddress(true)
                        .setSocketTcpNoDelay(true);

                AdvancedNetworkConfig advancedNetworkConfig = config.getAdvancedNetworkConfig();
                advancedNetworkConfig.setEnabled(true);
                advancedNetworkConfig.setJoin(joinConfig);
                advancedNetworkConfig.setMemberEndpointConfig(endpointConfig);

                HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance();
                Map<String, String> capitalcities = hzInstance.getMap( "capitals" );
                capitalcities.put( "1", "Tokyo" );
                System.out.println(capitalcities.get("1"));
                System.out.println(hzInstance.getCluster().getLocalMember().getAddressMap());
            }).run();
        }
    }

    private void startServer() throws IOException {
        //TODO after file DB
//        if(DBCtr.createLock()==false){
//            return;
//        }

        File exit = createExitFile();

        TransportLauncher.startTransports();

        while (true) {
            if (!exit.exists()) {
                try {
                    CoreRun.getInstance().shutdown();
                    Thread.sleep(500);
                    ShutdownManager.getInstance().shutdown();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
            //TODO after file DB
            //DBCtr.updateLock();
            ThreadUtil.sleep(1000);
        }
    }


    @NotNull
    private static File createExitFile() {
        File exit = new File(SysJMX.getProcessPID() + ".scouter");
        try {
            boolean created = exit.createNewFile();
            if (!created) {
                throw new RuntimeException("Fail to create scouter starter file. trying again.");
            }

        } catch (Exception e) {
            String tmp = System.getProperty("user.home", "/tmp");
            exit = new File(tmp, SysJMX.getProcessPID() + ".scouter.run");
            try {
                boolean created = exit.createNewFile();
                if (!created) {
                    throw new RuntimeException("Fail to create scouter starter file. trying again.");
                }

            } catch (Exception e2) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        exit.deleteOnExit();
        return exit;
    }
}
