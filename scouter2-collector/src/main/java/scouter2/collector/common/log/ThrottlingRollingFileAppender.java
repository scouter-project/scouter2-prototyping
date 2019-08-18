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

import ch.qos.logback.core.rolling.RollingFileAppender;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-17
 * <p>
 * Some log may be omitted by {@link ThrottleConfig} if the last logger argument type is a {@link ThrottleConfig}.
 */
public class ThrottlingRollingFileAppender<E> extends RollingFileAppender<E> {

    private Throttler<E> throttler = new Throttler<>();

    @Override
    protected void append(E eventObject) {
        if (!isStarted()) {
            return;
        }
        if (throttler.throttle(eventObject)) {
            return;
        }
        super.append(eventObject);
    }
}
