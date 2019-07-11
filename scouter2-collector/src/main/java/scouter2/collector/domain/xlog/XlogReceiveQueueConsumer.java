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
package scouter2.collector.domain.xlog;

import scouter2.collector.config.ConfigCommon;
import scouter2.common.util.ThreadUtil;
import scouter2.proto.Xlog;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
public class XlogReceiveQueueConsumer extends Thread {
    private static AtomicInteger threadNo = new AtomicInteger();
    private static XlogReceiveQueueConsumer instance;

    private ConfigCommon conf;
    private XlogReceiveQueue xlogReceiveQueue;
    private XlogAdder xlogAdder;

    public synchronized static XlogReceiveQueueConsumer start(ConfigCommon conf,
                                                              XlogReceiveQueue xlogReceiveQueue,
                                                              XlogAdder xlogAdder) {
        if (instance != null) {
            throw new RuntimeException("Already working xlog consumer exists.");
        }
        instance = new XlogReceiveQueueConsumer(conf, xlogReceiveQueue, xlogAdder);
        instance.setDaemon(true);
        instance.setName(ThreadUtil.getName(instance.getClass(), threadNo.getAndIncrement()));
        instance.start();
        return instance;
    }

    private XlogReceiveQueueConsumer(ConfigCommon conf, XlogReceiveQueue xlogReceiveQueue, XlogAdder xlogAdder) {
        this.conf = conf;
        this.xlogReceiveQueue = xlogReceiveQueue;
        this.xlogAdder = xlogAdder;
    }

    @Override
    public void run() {
        try {
            Xlog xlog = xlogReceiveQueue.take();
            xlogAdder.addXlog(xlog);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //TODO consume
    }
}
