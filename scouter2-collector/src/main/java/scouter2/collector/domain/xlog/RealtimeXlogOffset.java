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

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 30/08/2019
 */
@ToString
@Getter
public class RealtimeXlogOffset implements XlogOffset {

    long loop;
    int index;

    public RealtimeXlogOffset(long loop, int index) {
        this.loop = loop;
        this.index = index;
    }

    @Override
    public String serialize() {
        return loop + "_" + index;
    }

    @Override
    public XlogOffset deserialize(String serialized) {
        String[] split = StringUtils.split(serialized, '_');
        return new RealtimeXlogOffset(Long.parseLong(split[0]), Integer.parseInt(split[1]));
    }
}
