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
package scouter2.collector.domain.dict;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.primitive.IntInterval;
import org.springframework.stereotype.Component;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.main.CoreRun;
import scouter2.common.util.ThreadUtil;
import scouter2.proto.DictP;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Slf4j
public class DictReceiveQueueConsumer extends Thread {
    public static final ThrottleConfig S_0053 = ThrottleConfig.of("S0053");
    private static ImmutableList<DictReceiveQueueConsumer> consumerThreads;

    private DictReceiveQueue queue;
    private DictAdder adder;

    @Component
    public static class Runner {
        private static AtomicInteger threadNo = new AtomicInteger();
        private static final int dictReceiveQueueConsumerThreadCount = 1;

        public Runner(DictReceiveQueue receiveQueue,
                      DictAdder adder) {
            consumerThreads = IntInterval.zeroTo(dictReceiveQueueConsumerThreadCount - 1)
                    .collect(n -> createConsumer(receiveQueue, adder))
                    .toImmutable();
        }

        private DictReceiveQueueConsumer createConsumer(DictReceiveQueue receiveQueue,
                                                        DictAdder adder) {

            DictReceiveQueueConsumer consumer = new DictReceiveQueueConsumer(receiveQueue, adder);
            consumer.setDaemon(true);
            consumer.setName(ThreadUtil.getName(consumer.getClass(), threadNo.getAndIncrement()));
            consumer.start();

            return consumer;
        }
    }

    private DictReceiveQueueConsumer(DictReceiveQueue queue,
                                     DictAdder adder) {
        this.queue = queue;
        this.adder = adder;
    }

    @Override
    public void run() {
        while (CoreRun.isRunning()) {
            try {
                DictP dictP = queue.take();
                adder.record(dictP);

            } catch (Exception e) {
                log.error(e.getMessage(), S_0053, e);
            }
        }
    }
}
