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

package scouter2.collector;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import scouter2.collector.common.ShutdownManager;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.main.CoreRun;
import scouter2.collector.main.FileDbLockHandler;
import scouter2.common.helper.Props;
import scouter2.common.util.ScouterConfigUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static scouter2.collector.main.CollectorConstants.DEFAULT_CONF_DIR;
import static scouter2.collector.main.CollectorConstants.DEFAULT_CONF_FILE;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-26
 */
@Slf4j
public class CollectorApplication {
    private static Props loadProps;

    public static void main(String[] args) {
        log.info("starting Scouter CollectorApplication...");

        preloadConfig();
        preInit();
        if (!checkFileDbLock()) {
            log.error("can't check FileDbLock!");
            return;
        }
        SpringApplication.run(SpringBoot.class, args);
    }

    private static boolean checkFileDbLock() {
        ConfigCommon configCommon4Init = new ConfigCommon();
        configCommon4Init.refresh(loadProps);
        FileDbLockHandler.getInstance().setDbDir(configCommon4Init.getDbDir());
        return FileDbLockHandler.getInstance().createLock();
    }

    private static void preInit() {
        CoreRun.init();
        ShutdownManager.getInstance().register(() -> CoreRun.getInstance().shutdown()); //for manual shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> CoreRun.getInstance().shutdown())); //for kill signal
        Runtime.getRuntime().addShutdownHook(new Thread(() -> ShutdownManager.getInstance().shutdown())); //for kill signal
    }

    private static void preloadConfig() {
        String confFileName = System.getProperty("scouter2.config", DEFAULT_CONF_DIR + DEFAULT_CONF_FILE);
        if (StringUtils.isNotBlank(System.getenv("SCOUTER2_CONFIG"))) {
            confFileName = System.getenv("SCOUTER2_CONFIG");
        }

        File confFile = new File(confFileName);
        if (confFile.canRead()) {
            Properties configProps = new Properties();
            try (FileInputStream in = new FileInputStream(confFile)) {
                configProps.load(in);

            } catch (IOException e) {
                log.error(e.getMessage(), e);
                System.exit(-1);
            }

            Properties configWithSystemProps = ScouterConfigUtil.appendSystemProps(configProps);
            loadProps = new Props(configWithSystemProps);

            String repoType = System.getProperty("repoType");
            if (StringUtils.isBlank(repoType)) {
                System.setProperty("repoType", loadProps.getString("repoType"));
            }
        }
    }

    public static Props getLoadProps() {
        return loadProps;
    }
}
