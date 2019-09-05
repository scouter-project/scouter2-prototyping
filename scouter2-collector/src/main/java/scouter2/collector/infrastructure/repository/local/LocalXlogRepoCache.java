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

package scouter2.collector.infrastructure.repository.local;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.config.ConfigXlog;
import scouter2.collector.domain.xlog.XlogLoopCache;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 29/08/2019
 */
@Slf4j
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("local")
public class LocalXlogRepoCache extends Thread {

    private static final Map<String, XlogLoopCache> loopCacheMap = new ConcurrentHashMap<>();

    ConfigXlog configXlog;

    public LocalXlogRepoCache(ConfigXlog configXlog) {
        this.configXlog = configXlog;
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
            return xlogLoopCache;
        }
    }
}
