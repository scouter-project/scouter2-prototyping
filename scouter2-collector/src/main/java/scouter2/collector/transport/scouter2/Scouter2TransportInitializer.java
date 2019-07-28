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

package scouter2.collector.transport.scouter2;

import lombok.extern.slf4j.Slf4j;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.config.support.ConfigManager;
import scouter2.collector.transport.TransportInitializer;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-10
 */
@Slf4j
public class Scouter2TransportInitializer implements TransportInitializer {

    private Scouter2Transport transport;

    @Override
    public synchronized void start() {
        ConfigCommon config = ConfigManager.getConfig(ConfigCommon.class);
        transport = new Scouter2Transport(config.getNetTcpPort());
        transport.start();
    }

    @Override
    public void stop() {
        log.info("shutting down scouter2 transport since JVM is shutting down.");
        transport.stop();
        log.info("scouter2 transport shut down.");
    }
}
