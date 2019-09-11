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

package scouter2.collector.springconfig;

import scouter2.collector.common.util.U;
import scouter2.common.util.DateUtil;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 10/09/2019
 */
public final class ThreadNameDecorator {

    public static void runWithName(String threadName, Runnable runnable) {
        String[] threadNameParts = Thread.currentThread().getName().split("@@");
        String orgName = threadNameParts[0];
        String lastActionName = threadNameParts.length > 1 ? threadNameParts[1] : "none";
        String lastElapsedMillis = threadNameParts.length > 2 ? threadNameParts[2] : "none";
        long now = U.now();
        String hhmmss = DateUtil.hhmmss2(now);
        try {
            Thread.currentThread().setName(orgName + "@@" + threadName +
                    "@@(onRunning)" + hhmmss + "@@" + lastActionName + "@@elapsed:" + lastElapsedMillis);

            runnable.run();

        } finally {
            Thread.currentThread().setName(orgName + "@@(last)" + threadName +
                    "@@elapsed:" + (System.currentTimeMillis() - now));
        }
    }

    public static void run(Runnable runnable) {
        runWithName("run", runnable);
    }
}
