package scouter2.collector.main;

import scouter2.collector.common.ConfigPublisher;
import scouter2.collector.common.ConfigWatcher;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public class ServerBeanInitializer {
    private final static String DEFAULT_CONF_DIR = "./conf/";

    public static void init() {
        ConfigWatcher.start(System.getProperty("scouter.config", DEFAULT_CONF_DIR + "scouter.conf"),
                ConfigPublisher.getInstance());

    }

}
