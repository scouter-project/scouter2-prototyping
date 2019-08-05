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
package scouter2.collector.domain.instance;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.primitive.IntInterval;
import org.springframework.stereotype.Component;
import scouter2.collector.main.CoreRun;
import scouter2.common.util.ThreadUtil;
import scouter2.proto.InstanceP;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Slf4j
public class InstanceReceiveQueueConsumer extends Thread {
    private static ImmutableList<InstanceReceiveQueueConsumer> consumerThreads;

    private InstanceReceiveQueue queue;
    private InstanceAdder adder;
    private InstanceService service;
    private InstanceRepo repo;

    @Component
    public static class Runner {
        private static AtomicInteger threadNo = new AtomicInteger();
        private static final int instanceReceiveQueueConsumerThreadCount = 1;

        public Runner(InstanceReceiveQueue receiveQueue,
                      InstanceAdder adder,
                      InstanceService service,
                      InstanceRepo repo) {
            consumerThreads = IntInterval.zeroTo(instanceReceiveQueueConsumerThreadCount - 1)
                    .collect(n -> createConsumer(receiveQueue, adder, service, repo))
                    .toImmutable();
        }

        private InstanceReceiveQueueConsumer createConsumer(InstanceReceiveQueue receiveQueue,
                                                            InstanceAdder adder,
                                                            InstanceService service,
                                                            InstanceRepo repo) {

            InstanceReceiveQueueConsumer consumer = new InstanceReceiveQueueConsumer(receiveQueue, adder, service, repo);
            consumer.setDaemon(true);
            consumer.setName(ThreadUtil.getName(consumer.getClass(), threadNo.getAndIncrement()));
            consumer.start();

            return consumer;
        }
    }

    private InstanceReceiveQueueConsumer(InstanceReceiveQueue queue,
                                         InstanceAdder adder,
                                         InstanceService service,
                                         InstanceRepo repo) {
        this.queue = queue;
        this.adder = adder;
        this.service = service;
        this.repo = repo;
    }

    @Override
    public void run() {
        while (CoreRun.isRunning()) {
            try {
                InstanceP instanceProto = queue.take();
                long instanceId = instanceProto.getLegacyInstanceHash() != 0 ?
                        instanceProto.getLegacyInstanceHash()
                        : getInstanceHash(instanceProto.getInstanceFullName());

                Instance instance =  new Instance(instanceId, instanceProto) ;
                adder.addInstance(instance);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }

    private long getInstanceHash(String instanceFullName) {
        long instanceId = service.findIdByName(instanceFullName);
        if (instanceId == 0) {
            instanceId = repo.generateUniqueIdByName(instanceFullName);
        }
        return instanceId;
    }
}
