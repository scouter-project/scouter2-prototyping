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
package scouter2.collector.domain.dict;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import scouter2.common.collection.PurgingQueue;
import scouter2.proto.DictP;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Slf4j
@Component
public class DictReceiveQueue {
    private static DictReceiveQueue instance;
    private PurgingQueue<DictP> queue;

    public DictReceiveQueue() {
        synchronized (DictReceiveQueue.class) {
            if (instance != null) {
                throw new IllegalStateException();
            }
            this.queue = new PurgingQueue<>(10000);
            instance = this;
        }
    }

    /**
     * for 3rd party receiver support
     */
    public static DictReceiveQueue getInstance() {
        return instance;
    }

    public void offer(DictP dict) {
        boolean success = queue.offerOverflowClear(dict);
        if (!success) {
            //TODO logging
        }
    }

    public DictP take() throws InterruptedException {
        return queue.take();
    }
}
