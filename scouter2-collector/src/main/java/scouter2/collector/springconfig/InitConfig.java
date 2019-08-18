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

package scouter2.collector.springconfig;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import scouter2.collector.config.ConfigCommon;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-16
 */
@Configuration
@Slf4j
public class InitConfig {
    @Autowired
    ConfigCommon configCommon;

    @Bean
    public Initializer Initializer() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        Level.valueOf(configCommon.getLogRootLevel());
        log.info("ROOT log level changed: " + rootLogger.getLevel());

        Logger scouterLogger = (Logger) LoggerFactory.getLogger("scouter2");
        Level.valueOf(configCommon.getLogScouterLevel());
        log.info("SCOUTER log level changed: " + scouterLogger.getLevel());

        return new Initializer();
    }

    public static class Initializer {
        public void init() {
            //do nothing just for loading.
        }
    }
}
