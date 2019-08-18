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

package scouter2.collector.common.log;

import ch.qos.logback.classic.spi.LoggingEvent;
import lombok.ToString;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.slf4j.MDC;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-17
 */
class Throttler<E> {
    private static final String OMIT_MDC_KEY = "omit";
    private static final String OMIT_DEFAULT_VALUE = "-";
    MutableMap<String, LongBox> lastAccessMap = Maps.mutable.empty();
    MutableMap<String, LongBox> countMap = Maps.mutable.empty();

    boolean throttle(E eventObject) {

        if (eventObject instanceof LoggingEvent) {
            LoggingEvent e = (LoggingEvent) eventObject;
            Object[] args = e.getArgumentArray();
            if (args != null && args.length > 0) {
                Object candidate = args[args.length - 1];
                if (candidate instanceof ThrottleConfig) {
                    ThrottleConfig config = (ThrottleConfig) candidate;
                    MDC.put(OMIT_MDC_KEY, config.name);

                    LongBox count = countMap.computeIfAbsent(config.name, k -> new LongBox());
                    count.increase();

                    long logTs = e.getTimeStamp();
                    LongBox lastAccess = lastAccessMap.computeIfAbsent(config.name, k -> new LongBox(e.getTimeStamp()));
                    long lastAccessTs = lastAccess.value;
                    lastAccess.value = logTs;
                    long gapMillis = logTs - lastAccessTs;

                    if (config.intervalNotPassed(gapMillis)) {
                        if (count.value > config.thresholdCount) {
                            return true;
                        }

                    } else {
                        if (config.idlePassed(gapMillis)) {
                            count.value = 0;

                        } else if (count.value > config.thresholdCount + 1) {
                            String omitMessage = config.name
                                    + ":log-omitted(" +  (count.value - config.thresholdCount - 1) + ")";
                            MDC.put(OMIT_MDC_KEY, omitMessage);
                            count.value = config.thresholdCount;
                        }
                    }

                } else {
                    MDC.put(OMIT_MDC_KEY, OMIT_DEFAULT_VALUE);
                }

            } else {
                MDC.put(OMIT_MDC_KEY, OMIT_DEFAULT_VALUE);
            }

        } else {
            MDC.put(OMIT_MDC_KEY, OMIT_DEFAULT_VALUE);
        }
        return false;
    }

    @ToString
    private static class LongBox {
        protected long value;

        LongBox() {
        }

        LongBox(long value) {
            this.value = value;
        }

        void increase() {
            this.value += 1;
        }
    }
}
