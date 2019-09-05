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
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.springframework.stereotype.Component;
import scouter2.collector.config.ConfigXlog;
import scouter2.collector.main.CoreRun;
import scouter2.common.util.ThreadUtil;
import scouter2.proto.XlogP;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 29/08/2019
 */
@Slf4j
@Component
public class XlogLoopCacheManager extends Thread {

    private static XlogLoopCacheManager instance;

    private static AtomicInteger threadNo = new AtomicInteger();
    private static MutableList<XlogLoopCacheThread> mangerThreads = Lists.mutable.empty();
    private static final Map<String, XlogLoopCache> loopCacheMap = new ConcurrentHashMap<>();

    ConfigXlog configXlog;
    XlogRepo xlogRepo;

    public XlogLoopCacheManager(ConfigXlog configXlog, XlogRepo xlogRepo) {
        synchronized (XlogLoopCacheManager.class) {
            if (instance != null) {
                throw new IllegalStateException();
            }
            this.configXlog = configXlog;
            this.xlogRepo = xlogRepo;
            instance = this;
        }
    }

    public static XlogLoopCacheManager getInstance() {
        return instance;
    }

    public XlogLoopCache getCache(String applicationId) {
        XlogLoopCache xlogLoopCache = loopCacheMap.get(applicationId);
        if (xlogLoopCache != null) {
            return xlogLoopCache;
        }
        synchronized (this) {
            xlogLoopCache = loopCacheMap.get(applicationId);
            if (xlogLoopCache != null) {
                return xlogLoopCache;
            }
            xlogLoopCache = new XlogLoopCache(configXlog, applicationId);
            loopCacheMap.put(applicationId, xlogLoopCache);

            XlogLoopCacheThread thread = new XlogLoopCacheThread(applicationId, xlogLoopCache, xlogRepo, configXlog);
            thread.setDaemon(true);
            thread.setName(ThreadUtil.getName(thread.getClass(), applicationId, threadNo.getAndIncrement()));
            thread.start();
            return xlogLoopCache;
        }
    }

    @Slf4j
    public static class XlogLoopCacheThread extends Thread {
        ConfigXlog configXlog;
        String applicationId;
        XlogRepo xlogRepo;
        XlogOffset latestOffset;
        XlogLoopCache cache;

        XlogLoopCacheThread(String applicationId, XlogLoopCache cache, XlogRepo xlogRepo, ConfigXlog configXlog) {
            this.applicationId = applicationId;
            this.cache = cache;
            this.xlogRepo = xlogRepo;
            this.configXlog = configXlog;
        }

        @Override
        public void run() {
            while (CoreRun.isRunning()) {
                ThreadUtil.sleep(100);
                xlogRepo.streamLatest(applicationId, latestOffset, 10000, new XlogStreamObserver() {
                    @Override
                    public void onNext(XlogP xlogP) {
                        cache.add(xlogP);
                    }

                    @Override
                    public void onComplete(XlogOffset offset) {
                        latestOffset = offset;
                    }
                });
            }
        }
    }
}
