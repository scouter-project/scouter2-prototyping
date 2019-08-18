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
package scouter2.collector.config.support;

import lombok.extern.slf4j.Slf4j;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.main.CoreRun;
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

    public static final ThrottleConfig S_0025 = ThrottleConfig.of("S0025");
    private static AtomicInteger threadNo = new AtomicInteger();
    private static ConfigWatcher instance;

    private long lastLoaded = 0L;
    private File confFile;

    private ConfigManager publisher;

    public synchronized static ConfigWatcher start(String confFileName, ConfigManager publisher) {
        if (instance != null) {
            throw new RuntimeException("Already working ConfigWatcher exists.");
        }
        instance = new ConfigWatcher(confFileName, publisher);
        instance.setDaemon(true);
        instance.setName(ThreadUtil.getName(instance.getClass(), threadNo.getAndIncrement()));
        instance.reload();
        instance.start();
        return instance;
    }

    private ConfigWatcher(String confFileName, ConfigManager publisher) {
        confFile = new File(confFileName);
        this.publisher = publisher;
    }

    @Override
    public void run() {
        while (CoreRun.isRunning()) {
            ThreadUtil.sleep(3000);
            reload();
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
                log.error("Error on reading config file. file: {}", confFile.getAbsolutePath(), S_0025, e);
            }

            Properties configWithSystemProps = ScouterConfigUtil.appendSystemProps(configProps);
            publisher.refresh(configWithSystemProps);
            return true;
        }
        return false;
    }
}
