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

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-17
 *
 * Throtting option for ThrottlingXXXAppenders like {@link ThrottlingRollingFileAppender}
 * After exceeding the thresholdCount in the timeToIdleSec omit logging within intervalMillis.
 * No calling(logging) within the timeToIdleSec, the thresholdCount cleared.
 */
public class ThrottleConfig {

    protected String name = "default";
    protected int thresholdCount = 10;
    protected long intervalMillis = 5000;
    protected long timeToIdleMillis = 10000;

    private ThrottleConfig() {}

    public static ThrottleConfig of(String name) {
        ThrottleConfig throttler = new ThrottleConfig();
        throttler.name = name;
        return throttler;
    }

    public static ThrottleConfig of(String name, long intervalMillis) {
        ThrottleConfig throttler = new ThrottleConfig();
        throttler.name = name;
        throttler.intervalMillis = intervalMillis;
        return throttler;
    }

    public static ThrottleConfig of(String name, long intervalMillis, int thresholdCount) {
        ThrottleConfig throttler = new ThrottleConfig();
        throttler.name = name;
        throttler.intervalMillis = intervalMillis;
        throttler.thresholdCount = thresholdCount;
        return throttler;
    }

    public static ThrottleConfig of(String name, long intervalMillis, int thresholdCount, long timeToIdleMillis) {
        ThrottleConfig throttler = new ThrottleConfig();
        throttler.name = name;
        throttler.intervalMillis = intervalMillis;
        throttler.timeToIdleMillis = timeToIdleMillis;
        return throttler;
    }

    public boolean idlePassed(long timeGapMillis) {
        return timeGapMillis > timeToIdleMillis;
    }

    public boolean intervalNotPassed(long timeGapMillis) {
        return timeGapMillis < intervalMillis;
    }
}
