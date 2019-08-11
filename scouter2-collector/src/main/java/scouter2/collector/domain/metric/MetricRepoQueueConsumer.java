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
package scouter2.collector.domain.metric;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.list.primitive.IntInterval;
import org.springframework.stereotype.Component;
import scouter2.collector.config.ConfigMetric;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.collector.main.CoreRun;
import scouter2.common.util.ThreadUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Slf4j
public class MetricRepoQueueConsumer extends Thread {
    private static ImmutableList<MetricRepoQueueConsumer> consumerThreads;

    ConfigMetric conf;
    MetricRepoQueue repoQueue;
    MetricRepo repo;
    MetricService metricService;
    ObjService objService;

    @Component
    public static class Runner {
        private static AtomicInteger threadNo = new AtomicInteger();

        public Runner(ConfigMetric conf,
                      MetricRepoQueue metricRepoQueue,
                      MetricRepo repo,
                      MetricService metricService,
                      ObjService objService) {

            consumerThreads = IntInterval.zeroTo(getConsumerCount(conf, repo) - 1)
                    .collect(n -> createConsumer(conf, metricRepoQueue, repo, metricService, objService))
                    .toImmutable();
        }

        private MetricRepoQueueConsumer createConsumer(ConfigMetric conf,
                                                       MetricRepoQueue repoQueue,
                                                       MetricRepo repo,
                                                       MetricService metricService,
                                                       ObjService objService) {

            MetricRepoQueueConsumer consumer =
                    new MetricRepoQueueConsumer(conf, repoQueue, repo, metricService, objService);
            consumer.setDaemon(true);
            consumer.setName(ThreadUtil.getName(consumer.getClass(), threadNo.getAndIncrement()));
            consumer.start();

            return consumer;
        }
    }

    /**
     * always 1 on NoneThreadSafeXlogRepo type
     */
    private static int getConsumerCount(ConfigMetric conf, MetricRepo repo) {
        return repo instanceof NonThreadSafeRepo ? 1 : conf.getMetricRepoThreadCount();
    }

    private MetricRepoQueueConsumer(ConfigMetric conf,
                                    MetricRepoQueue repoQueue,
                                    MetricRepo repo,
                                    MetricService metricService,
                                    ObjService objService) {
        this.conf = conf;
        this.repoQueue = repoQueue;
        this.repo = repo;
        this.metricService = metricService;
        this.objService = objService;
    }

    @Override
    public void run() {
        while (CoreRun.isRunning()) {
            try {
                Metric metric = repoQueue.take();

                addToRepo(metric);
                continue;

            } catch (Throwable t) {
                t.printStackTrace();
                log.error(t.getMessage(), t);
            }
        }
    }

    private void addToRepo(Metric metric) {
        long objId = objService.findIdByName(metric.getProto().getObjFullName());
        if (objId == 0) return;

        Obj obj = objService.findById(objId);
        if (obj == null) return;

        repo.add(obj.getProto().getApplicationId(),
                metric.toRepoType(objId, metricService, repo instanceof NonThreadSafeRepo));
    }
}
