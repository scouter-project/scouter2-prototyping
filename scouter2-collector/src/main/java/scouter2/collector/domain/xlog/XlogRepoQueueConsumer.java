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

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.primitive.IntInterval;
import scouter2.collector.config.ConfigXlog;
import scouter2.common.util.ThreadUtil;
import scouter2.proto.Xlog;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Slf4j
public class XlogRepoQueueConsumer extends Thread {
    private static AtomicInteger threadNo = new AtomicInteger();
    private static ImmutableList<XlogRepoQueueConsumer> instances;

    ConfigXlog conf;
    XlogRepoQueue xlogRepoQueue;
    XlogRepo xlogRepo;

    public synchronized static ImmutableList<XlogRepoQueueConsumer> start(@Nonnull ConfigXlog conf,
                                                                          @Nonnull XlogRepoQueue xlogRepoQueue,
                                                                          @Nonnull XlogRepo xlogRepo) {

        if (instances != null) {
            throw new RuntimeException("Already working xlog repo queue consumer exists.");
        }

        instances = IntInterval.zeroTo(getConsumerCount(conf, xlogRepo) - 1)
                .collect(n -> createXlogRepoQueueConsumer(conf, xlogRepoQueue, xlogRepo))
                .toImmutable();

        return instances;
    }

    private static XlogRepoQueueConsumer createXlogRepoQueueConsumer(ConfigXlog conf, XlogRepoQueue xlogRepoQueue, XlogRepo xlogRepo) {
        XlogRepoQueueConsumer consumer = new XlogRepoQueueConsumer(conf, xlogRepoQueue, xlogRepo);
        consumer.setDaemon(true);
        consumer.setName(ThreadUtil.getName(consumer.getClass(), threadNo.getAndIncrement()));
        consumer.start();

        return consumer;
    }

    /**
     * always 1 on NoneThreadSafeXlogRepo type
     */
    private static int getConsumerCount(ConfigXlog conf, XlogRepo xlogRepo) {
        return xlogRepo instanceof NoneThreadSafeXlogRepo ? 1 : conf.getXlogRepoThreadCount();
    }

    private XlogRepoQueueConsumer(ConfigXlog conf, XlogRepoQueue xlogRepoQueue, XlogRepo xlogRepo) {
        this.conf = conf;
        this.xlogRepoQueue = xlogRepoQueue;
        this.xlogRepo = xlogRepo;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Xlog xlog = xlogRepoQueue.take();
                xlogRepo.add(xlog);

            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            }
        }
    }
}
