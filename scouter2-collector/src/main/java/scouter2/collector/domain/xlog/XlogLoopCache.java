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
import org.eclipse.collections.api.set.primitive.LongSet;
import scouter2.collector.config.ConfigXlog;
import scouter2.proto.XlogP;

/**
 * Cache of latest XLog data
 *
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-29
 */
@Slf4j
public class XlogLoopCache {

    private final String applicationId;
    private final int maxRetrieveCount;
    private final ConfigXlog configXlog;

    private long loop = 0;
    private int index = 0;
    private final XlogP[] queue;

    public XlogLoopCache(ConfigXlog configXlog, String applicationId) {
        this.applicationId = applicationId;
        this.maxRetrieveCount = configXlog.getXlogLoopCacheSize() / 2;
        this.configXlog = configXlog;
        queue = new XlogP[configXlog.getXlogLoopCacheSize()];
    }

    public void add(XlogP xlogP) {
        synchronized (queue) {
            queue[index] = xlogP;
            index += 1;
            if (index >= queue.length) {
                loop += 1;
                index = 0;
            }
        }
    }

    /**
     * get new xlog data revealed after the previous check point, and invoke the handler consumer
     *
     * @param aLoop - last loop retrieved
     * @param aIndex - last index retrieved
     */
    public void getAndHandleRealTimeXLog(LongSet objIds, long aLoop, int aIndex, int aMaxCount,
                                         RealtimeXlogStreamObserver stream) {
        int maxCount = aMaxCount;

        //Initial call
        if(aLoop == 0 && aIndex == 0) {
        } else if(aIndex + 1 >= queue.length) {
            aIndex = 0;
            aLoop++;
        }

        long currentLoop;
        int currentIndex;
        synchronized (queue) {
            currentLoop = this.loop;
            currentIndex = this.index;
        }

        int loopStatus = (int) (currentLoop - aLoop);

        switch (loopStatus) {
            case 0:
                int countToGet = currentIndex - aIndex;
                if (aIndex < currentIndex) {
                    maxCount = Math.min(aMaxCount, countToGet);
                } else {
                    RealtimeXlogOffset lastOffset = new RealtimeXlogOffset(currentLoop, currentIndex);
                    stream.onLoad(lastOffset);
                    stream.onComplete(lastOffset);
                    return;
                }
                break;

            case 1:
                maxCount = Math.min(aMaxCount, queue.length - aIndex + currentIndex);
                break;
            default:
        }

        if (maxCount == 0 || maxCount > maxRetrieveCount) {
            maxCount = maxRetrieveCount;
        }

        RealtimeXlogOffset lastOffset = new RealtimeXlogOffset(currentLoop, currentIndex);
        stream.onLoad(lastOffset);
        if (maxCount > currentIndex) {
            handleInternal(objIds, queue.length - (maxCount - currentIndex), queue.length, stream);
            handleInternal(objIds, 0, currentIndex, stream);

        } else {
            handleInternal(objIds, currentIndex - maxCount, currentIndex, stream);
        }
        stream.onComplete(lastOffset);
    }

    /**
     * get xlog data and invoke consumer within the range
     *
     * @param from
     * @param to
     * @param stream
     */
    private void handleInternal(LongSet objIds, int from, int to, RealtimeXlogStreamObserver stream) {
        for (int i = from; i < to; i++) {
            XlogP xlogP = queue[i];

            //filter objHash
            if (objIds != null && objIds.size() > 0 && !objIds.contains(xlogP.getObjId())) {
                continue;
            }
            stream.onNext(xlogP);
        }
    }
}
