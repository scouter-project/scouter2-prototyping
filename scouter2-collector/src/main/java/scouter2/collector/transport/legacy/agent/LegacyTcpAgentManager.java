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

package scouter2.collector.transport.legacy.agent;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scouter2.collector.config.ConfigLegacy;
import scouter2.collector.springconfig.ThreadNameDecorator;
import scouter2.common.util.ThreadUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 10/09/2019
 */
@Component
public class LegacyTcpAgentManager {

    private static LegacyTcpAgentManager instance;
    private static ConcurrentHashMap<Long, LinkedBlockingQueue<LegacyTcpAgentWorker>> agentTable =
            new ConcurrentHashMap<>();
    private static ExecutorService es =
            ThreadUtil.createExecutor("SCOUTER-LegacyTcpAgentManagerSessionChecker", 5, 5, Integer.MAX_VALUE, true);

    ConfigLegacy configLegacy;

    public LegacyTcpAgentManager(ConfigLegacy configLegacy) {
        synchronized (LegacyTcpAgentManager.class) {
            if (instance != null) {
                throw new IllegalStateException();
            }
            this.configLegacy = configLegacy;
            instance = this;
        }
    }

    public static LegacyTcpAgentManager getInstance() {
        return instance;
    }

    public int add(long objLegacyHash, LegacyTcpAgentWorker worker) {
        synchronized (agentTable) {
            LinkedBlockingQueue<LegacyTcpAgentWorker> sessions =
                    agentTable.computeIfAbsent(objLegacyHash, k -> new LinkedBlockingQueue<>());
            sessions.add(worker);
            return sessions.size();
        }
    }

    public LegacyTcpAgentWorker get(long objLegacyHash) {
        LinkedBlockingQueue<LegacyTcpAgentWorker> workers = agentTable.get(objLegacyHash);
        if (workers == null) {
            return null;
        }
        try {
            return workers.poll(configLegacy.getLegacyNetTcpGetAgentConnectionWaitMs(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void allSessionCheck() {
        ThreadNameDecorator.runWithName(this.getClass().getSimpleName(), () -> {
            for (LinkedBlockingQueue<LegacyTcpAgentWorker> agentSessionQueue : agentTable.values()) {
                es.execute(() -> sessionCheckOfAgent(agentSessionQueue));
            };
        });
    }

    private void sessionCheckOfAgent(LinkedBlockingQueue<LegacyTcpAgentWorker> agentSessionQueue) {
        ThreadNameDecorator.run(() -> {
            for (int i = 0; i < agentSessionQueue.size(); i++) {
                LegacyTcpAgentWorker worker = agentSessionQueue.poll();
                if (worker == null) {
                    break;
                }
                if (!worker.isClosed()) {
                    if (worker.isExpired()) {
                        worker.sendKeepAlive(3000);
                    }
                    agentSessionQueue.add(worker);
                }
            }
        });
    }
}
