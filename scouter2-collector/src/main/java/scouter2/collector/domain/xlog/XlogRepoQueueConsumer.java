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
import org.springframework.stereotype.Component;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigXlog;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.main.CoreRun;
import scouter2.common.util.ThreadUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Slf4j
public class XlogRepoQueueConsumer extends Thread {
    public static final ThrottleConfig S_0010 = ThrottleConfig.of("S0010");
    private static ImmutableList<XlogRepoQueueConsumer> consumerThreads;

    ConfigXlog conf;
    XlogRepoQueue repoQueue;
    XlogRepo repo;

    @Component
    public static class Runner {
        private static AtomicInteger threadNo = new AtomicInteger();

        public Runner(ConfigXlog conf, XlogRepoQueue xlogRepoQueue, XlogRepo xlogRepo) {
            consumerThreads = IntInterval.zeroTo(getConsumerCount(conf, xlogRepo) - 1)
                    .collect(n -> createConsumer(conf, xlogRepoQueue, xlogRepo))
                    .toImmutable();
        }

        private XlogRepoQueueConsumer createConsumer(ConfigXlog conf, XlogRepoQueue xlogRepoQueue, XlogRepo xlogRepo) {
            XlogRepoQueueConsumer consumer = new XlogRepoQueueConsumer(conf, xlogRepoQueue, xlogRepo);
            consumer.setDaemon(true);
            consumer.setName(ThreadUtil.getName(consumer.getClass(), threadNo.getAndIncrement()));
            consumer.start();

            return consumer;
        }
    }

    /**
     * always 1 on NoneThreadSafeXlogRepo type
     */
    private static int getConsumerCount(ConfigXlog conf, XlogRepo xlogRepo) {
        return xlogRepo instanceof NonThreadSafeRepo ? 1 : conf.getXlogRepoThreadCount();
    }

    private XlogRepoQueueConsumer(ConfigXlog conf, XlogRepoQueue repoQueue, XlogRepo repo) {
        this.conf = conf;
        this.repoQueue = repoQueue;
        this.repo = repo;
    }

    @Override
    public void run() {
        while (CoreRun.isRunning()) {
            try {
                Xlog xlog = repoQueue.take();
                repo.add(xlog.getApplicationId(), xlog);

            } catch (InterruptedException e) {
                log.error(e.getMessage(), S_0010, e);
            }
        }
    }
}
