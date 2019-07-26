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
package scouter2.collector.beanfactory;

import lombok.extern.slf4j.Slf4j;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.config.ConfigPublisher;
import scouter2.collector.config.ConfigWatcher;
import scouter2.collector.config.ConfigXlog;
import scouter2.collector.domain.xlog.XlogAdder;
import scouter2.collector.domain.xlog.XlogReceiveQueue;
import scouter2.collector.domain.xlog.XlogReceiveQueueConsumer;
import scouter2.collector.domain.xlog.XlogRepo;
import scouter2.collector.domain.xlog.XlogRepoQueue;
import scouter2.collector.domain.xlog.XlogRepoQueueConsumer;
import scouter2.collector.infrastructure.repository.MutingXlogRepo;
import scouter2.common.config.ScouterConfigIF;

import java.util.Set;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
@Slf4j
public class ServerBeanInitializer {
    private final static String DEFAULT_CONF_DIR = "./conf/";

    public static void init() {
        ConfigPublisher configPublisher = registerConfigPublisher();
        registerConfigBeans();
        startConfigWatcher(configPublisher);

        ConfigCommon configCommon = SingleBeanFactory.getBean(ConfigCommon.class);
        ConfigXlog configXlog = SingleBeanFactory.getBean(ConfigXlog.class);

        registerAndInitXlogProcessors(configCommon, configXlog);
    }

    private static void startConfigWatcher(ConfigPublisher configPublisher) {
        ConfigWatcher.start(System.getProperty("scouter.config", DEFAULT_CONF_DIR + "scouter.conf"),
                configPublisher);
    }

    private static ConfigPublisher registerConfigPublisher() {
        ConfigPublisher configPublisher = new ConfigPublisher();
        SingleBeanFactory.addBean(ConfigPublisher.class, configPublisher);

        return configPublisher;
    }

    private static void registerConfigBeans() {
        Set<String> classes = new Scanner("scouter2.collector.config").process();
        Set<String> custom = new Scanner(System.getProperty("scouter2.config.package")).process();
        classes.addAll(custom);

        for (String aClass : classes) {
            try {
                Class<?> clazz = Class.forName(aClass);
                if (!ScouterConfigIF.class.isAssignableFrom(clazz)) {
                    continue;
                }
                ScouterConfigIF config = (ScouterConfigIF) clazz.newInstance();
                SingleBeanFactory.addBean(clazz, config);
                ConfigPublisher configPublisher = SingleBeanFactory.getBean(ConfigPublisher.class);
                configPublisher.register(config);

            } catch (Exception e) {
                log.error("Exception on loading scouter config classes", e);
            }
        }
    }

    private static void registerAndInitXlogProcessors(ConfigCommon configCommon, ConfigXlog configXlog) {
        XlogRepo xlogRepo = new MutingXlogRepo();
        XlogRepoQueue xlogRepoQueue = new XlogRepoQueue(configXlog);

        XlogRepoQueueConsumer.start(configXlog, xlogRepoQueue, xlogRepo);

        XlogAdder xlogAdder = new XlogAdder(configCommon, xlogRepoQueue);
        XlogReceiveQueue xlogReceiveQueue = new XlogReceiveQueue(configXlog);
        XlogReceiveQueueConsumer.start(configCommon, xlogReceiveQueue, xlogAdder);

        SingleBeanFactory.addBean(XlogRepo.class, xlogRepo);
        SingleBeanFactory.addBean(XlogRepoQueue.class, xlogRepoQueue);
        SingleBeanFactory.addBean(XlogAdder.class, xlogAdder);
        SingleBeanFactory.addBean(XlogReceiveQueue.class, xlogReceiveQueue);

    }
}
