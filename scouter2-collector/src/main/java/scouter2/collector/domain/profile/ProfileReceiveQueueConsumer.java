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
package scouter2.collector.domain.profile;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.primitive.IntInterval;
import org.springframework.stereotype.Component;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigXlog;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.main.CoreRun;
import scouter2.common.util.ThreadUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Slf4j
public class ProfileReceiveQueueConsumer extends Thread {
    public static final ThrottleConfig S_0063 = ThrottleConfig.of("S0063");
    private static ImmutableList<ProfileReceiveQueueConsumer> consumerThreads;

    private ConfigXlog conf;
    private ProfileReceiveQueue receiveQueue;
    private ProfileAdder adder;
    private ObjService objService;

    @Component
    public static class Runner {
        private static AtomicInteger threadNo = new AtomicInteger();

        public Runner(ConfigXlog conf, ProfileReceiveQueue receiveQueue, ProfileAdder adder) {
            consumerThreads = IntInterval.zeroTo(conf.getXlogReceiverThreadCount() - 1)
                    .collect(n -> createConsumer(conf, receiveQueue, adder))
                    .toImmutable();
        }

        private ProfileReceiveQueueConsumer createConsumer(ConfigXlog conf,
                                                           ProfileReceiveQueue receiveQueue,
                                                           ProfileAdder adder) {

            ProfileReceiveQueueConsumer consumer = new ProfileReceiveQueueConsumer(conf, receiveQueue, adder);
            consumer.setDaemon(true);
            consumer.setName(ThreadUtil.getName(consumer.getClass(), threadNo.getAndIncrement()));
            consumer.start();

            return consumer;
        }
    }

    private ProfileReceiveQueueConsumer(ConfigXlog conf, ProfileReceiveQueue receiveQueue, ProfileAdder adder) {
        this.conf = conf;
        this.receiveQueue = receiveQueue;
        this.adder = adder;
    }

    @Override
    public void run() {
        while (CoreRun.isRunning()) {
            try {
                Profile profile = receiveQueue.take();
                adder.add(profile);

            } catch (InterruptedException e) {
                log.error(e.getMessage(), S_0063, e);
            }
        }
    }
}
