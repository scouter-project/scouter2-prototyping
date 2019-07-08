package scouter2.collector.main;

import scouter2.collector.common.ShutdownManager;
import scouter2.common.util.SysJMX;
import scouter2.common.util.ThreadUtil;

import java.io.File;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
public class CollectorMain {

    public static void main(String[] args) {
        //TODO logo
        ServerBeanInitializer.init();
        startServer();
    }

    private static void startServer() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> ShutdownManager.getInstance().shutdown()));

        //TODO after file DB
//        if(DBCtr.createLock()==false){
//            return;
//        }

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
        System.out.println("System JRE version : " + System.getProperty("java.version"));

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
}
