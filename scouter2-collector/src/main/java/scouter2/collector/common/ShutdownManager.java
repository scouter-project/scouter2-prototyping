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
package scouter2.collector.common;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
@Slf4j
public class ShutdownManager {

    private static ShutdownManager instance = new ShutdownManager();
    LinkedList<ShutdownHook> shutdownHooks = new LinkedList<>();
    LinkedList<ShutdownHook> shutdownHooks1st = new LinkedList<>();

    private ShutdownManager() {}

    public static ShutdownManager getInstance() {
        return instance;
    }

    public void register(ShutdownHook shutdownHook) {
        shutdownHooks.add(shutdownHook);
    }

    public void register1st(ShutdownHook shutdownHook) {
        shutdownHooks1st.add(shutdownHook);
    }

    public synchronized void shutdown() {
        log.info("Collector on shutdown.");
        try {
            //waiting processing
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (ShutdownHook hook : shutdownHooks1st) {
            try {
                hook.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        shutdownHooks1st.clear();
        for (ShutdownHook hook : shutdownHooks) {
            try {
                hook.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        shutdownHooks.clear();
    }
}
