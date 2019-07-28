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

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.plugin.Scouter2PluginMeta;
import scouter2.collector.transport.TransportInitializer;
import scouter2.common.util.SysJMX;
import scouter2.common.util.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Component
@Slf4j
public class CollectorMain {

    ServerBeanInitializer serverBeanInitializer;

    public CollectorMain(ServerBeanInitializer serverBeanInitializer) {
        this.serverBeanInitializer = serverBeanInitializer;
    }

    public void start(String[] args) throws IOException {
        //TODO logo
        System.out.println("System JRE version : " + System.getProperty("java.version"));
        serverBeanInitializer.init();
        startServer();
    }

    private void startServer() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> ShutdownManager.getInstance().shutdown()));

        //TODO after file DB
//        if(DBCtr.createLock()==false){
//            return;
//        }

        File exit = createExitFile();
        startTransports();

        while (true) {
            if (!exit.exists()) {
                ShutdownManager.getInstance().shutdown();
                System.exit(0);
            }
            //TODO after file DB
            //DBCtr.updateLock();
            ThreadUtil.sleep(1000);
        }
    }

    private static void startTransports() {
        startScouterTransport();
        start3rdPartyTransport();
    }

    private static void startScouterTransport() {
        Set<String> classes = new Scanner(TransportInitializer.class.getPackage().getName()).process();
        startTransport0(classes);
    }

    private static void start3rdPartyTransport() {
        ServiceLoader<Scouter2PluginMeta> loader = ServiceLoader.load(Scouter2PluginMeta.class);
        for (Scouter2PluginMeta meta : loader) {
            Set<String> classes = new Scanner(meta.getComponentScanPackage()).process();
            startTransport0(classes);
        }
    }

    private static void startTransport0(Set<String> classes) {
        for (String aClass : classes) {
            try {
                Class<?> clazz = Class.forName(aClass);
                if (!TransportInitializer.class.isAssignableFrom(clazz)) {
                    continue;
                }
                if (TransportInitializer.class == clazz) {
                    continue;
                }
                TransportInitializer transport = (TransportInitializer) clazz.newInstance();
                transport.start();
                ShutdownManager.getInstance().register(transport::stop);

            } catch (Exception e) {
                log.error("Exception on loading scouter transport classes", e);
            }
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
