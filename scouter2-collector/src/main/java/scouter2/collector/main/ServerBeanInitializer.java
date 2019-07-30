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
import org.springframework.stereotype.Component;
import scouter2.collector.config.support.ConfigManager;
import scouter2.collector.config.support.ConfigWatcher;
import scouter2.collector.plugin.Scouter2PluginMeta;
import scouter2.common.config.ScouterConfigIF;

import javax.annotation.PostConstruct;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
@Slf4j
@Component
public class ServerBeanInitializer {
    ConfigManager configManager;

    public ServerBeanInitializer(ConfigManager configPublisher) {
        this.configManager = configPublisher;
    }

    @PostConstruct
    public void init() {
        registerCustomConfigBeans();
        startConfigWatcher();
    }

    private void registerCustomConfigBeans() {
        ServiceLoader<Scouter2PluginMeta> loader = ServiceLoader.load(Scouter2PluginMeta.class);
        for (Scouter2PluginMeta meta : loader) {
            Set<String> classes = new Scanner(meta.getComponentScanPackage()).process();
            for (String aClass : classes) {
                try {
                    Class<?> clazz = Class.forName(aClass);
                    if (!ScouterConfigIF.class.isAssignableFrom(clazz)) {
                        continue;
                    }
                    if (ScouterConfigIF.class == clazz) {
                        continue;
                    }
                    ScouterConfigIF config = (ScouterConfigIF) clazz.newInstance();
                    configManager.register(config);

                } catch (Exception e) {
                    log.error("Exception on loading scouter config classes", e);
                }
            }
        }
    }

    private void startConfigWatcher() {
        ConfigWatcher.start(System.getProperty("scouter2.config",
                CollectorConstants.DEFAULT_CONF_DIR + CollectorConstants.DEFAULT_CONF_FILE), configManager);
    }

}
