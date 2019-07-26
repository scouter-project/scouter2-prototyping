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
import scouter2.common.util.ThreadUtil;
import scouter2.proto.Instance;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Slf4j
public class InstanceReceiveQueueConsumer extends Thread {
    private static AtomicInteger threadNo = new AtomicInteger();
    private static InstanceReceiveQueueConsumer instance;

    private InstanceReceiveQueue queue;
    private InstanceAdder instanceAdder;

    public synchronized static InstanceReceiveQueueConsumer start(InstanceReceiveQueue receiveQueue,
                                                                  InstanceAdder instanceAdder) {
        if (instance != null) {
            throw new RuntimeException("Already working InstanceReceiveQueueConsumer exists.");
        }
        instance = new InstanceReceiveQueueConsumer(receiveQueue, instanceAdder);
        instance.setDaemon(true);
        instance.setName(ThreadUtil.getName(instance.getClass(), threadNo.getAndIncrement()));
        instance.start();
        return instance;
    }

    private InstanceReceiveQueueConsumer(InstanceReceiveQueue queue, InstanceAdder instanceAdder) {
        this.queue = queue;
        this.instanceAdder = instanceAdder;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Instance instance = queue.take();
                instanceAdder.addInstance(instance);

            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }
}
