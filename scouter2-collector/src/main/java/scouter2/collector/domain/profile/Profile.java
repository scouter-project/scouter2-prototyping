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

package scouter2.collector.domain.profile;

import lombok.Getter;
import scouter2.proto.ProfileP;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 07/09/2019
 */
@Getter
public class Profile {
    ProfileP proto;
    long timestamp;

    public Profile(ProfileP proto) {
        this(proto, System.currentTimeMillis());
    }

    public Profile(ProfileP proto, long timestamp) {
        this.proto = proto;
        this.timestamp = timestamp;
    }
}
