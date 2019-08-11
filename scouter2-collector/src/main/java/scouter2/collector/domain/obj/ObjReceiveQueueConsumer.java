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
package scouter2.collector.domain.obj;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.primitive.IntInterval;
import org.springframework.stereotype.Component;
import scouter2.collector.main.CoreRun;
import scouter2.common.util.ThreadUtil;
import scouter2.proto.ObjP;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Slf4j
public class ObjReceiveQueueConsumer extends Thread {
    private static ImmutableList<ObjReceiveQueueConsumer> consumerThreads;

    private ObjReceiveQueue queue;
    private ObjAdder adder;
    private ObjService service;
    private ObjRepo repo;

    @Component
    public static class Runner {
        private static AtomicInteger threadNo = new AtomicInteger();
        private static final int instanceReceiveQueueConsumerThreadCount = 1;

        public Runner(ObjReceiveQueue receiveQueue,
                      ObjAdder adder,
                      ObjService service,
                      ObjRepo repo) {
            consumerThreads = IntInterval.zeroTo(instanceReceiveQueueConsumerThreadCount - 1)
                    .collect(n -> createConsumer(receiveQueue, adder, service, repo))
                    .toImmutable();
        }

        private ObjReceiveQueueConsumer createConsumer(ObjReceiveQueue receiveQueue,
                                                       ObjAdder adder,
                                                       ObjService service,
                                                       ObjRepo repo) {

            ObjReceiveQueueConsumer consumer = new ObjReceiveQueueConsumer(receiveQueue, adder, service, repo);
            consumer.setDaemon(true);
            consumer.setName(ThreadUtil.getName(consumer.getClass(), threadNo.getAndIncrement()));
            consumer.start();

            return consumer;
        }
    }

    private ObjReceiveQueueConsumer(ObjReceiveQueue queue,
                                    ObjAdder adder,
                                    ObjService service,
                                    ObjRepo repo) {
        this.queue = queue;
        this.adder = adder;
        this.service = service;
        this.repo = repo;
    }

    @Override
    public void run() {
        while (CoreRun.isRunning()) {
            try {
                ObjP objP = queue.take();
                long instanceId = objP.getLegacyObjHash() != 0 ?
                        objP.getLegacyObjHash()
                        : findInstanceId(objP.getObjFullName());

                Obj obj =  new Obj(instanceId, objP) ;
                adder.addObj(obj);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }

    private long findInstanceId(String objFullName) {
        long objId = service.findIdByName(objFullName);
        if (objId == 0) {
            objId = service.generateUniqueIdByName(objFullName);
        }
        return objId;
    }
}