package scouter2.collector.common;

import lombok.extern.slf4j.Slf4j;
import scouter2.common.util.ScouterConfigUtil;
import scouter2.common.util.ThreadUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
@Slf4j
public class ConfigWatcher extends Thread {

    private static AtomicInteger threadNo = new AtomicInteger();
    private static ConfigWatcher instance;

    private long lastLoaded = 0L;
    private File confFile;

    private ConfigPublisher publisher;

    public synchronized static ConfigWatcher start(String confDir, ConfigPublisher publisher) {
        if (instance != null) {
            throw new RuntimeException("Already working ConfigWatcher exists.");
        }
        instance = new ConfigWatcher(confDir, publisher);
        instance.setDaemon(true);
        instance.setName(ThreadUtil.getName(instance.getClass(), threadNo.getAndIncrement()));
        instance.start();
        return instance;
    }

    //System.getProperty("scouter.config", DEFAULT_CONF_DIR + "scouter.conf")
    private ConfigWatcher(String confFileName, ConfigPublisher publisher) {
        confFile = new File(confFileName);
        this.publisher = publisher;
    }

    @Override
    public void run() {
        while (true) {
            reload();
            ThreadUtil.sleep(3000);
        }
    }

    private boolean reload() {
        if (confFile.lastModified() == lastLoaded) {
            return false;
        }

        lastLoaded = confFile.lastModified();
        if (confFile.canRead()) {
            Properties configProps = new Properties();
            try (FileInputStream in = new FileInputStream(confFile)) {
                configProps.load(in);
            } catch (Exception e) {
                log.error("Error on reading config file. file: {}", confFile.getAbsolutePath(), e);
            }

            Properties configWithSystemProps = ScouterConfigUtil.appendSystempProps(configProps);
            publisher.refresh(configWithSystemProps);
            return true;
        }
        return false;
    }
}
