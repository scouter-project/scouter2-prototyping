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

package scouter2.testsupport;

import com.google.protobuf.ByteString;
import scouter2.common.support.XlogIdSupport;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 05/09/2019
 */
public final class T {

    public static ByteString xlogIdAsBs() {
        return ByteString.copyFrom(XlogIdSupport.createId());
    }

    public static ByteString xlogIdAsBs(long timestamp) {
        return ByteString.copyFrom(XlogIdSupport.createId(timestamp));
    }

    public static byte[] xlogId() {
        return XlogIdSupport.createId();
    }

    public static byte[] xlogId(long timestamp) {
        return XlogIdSupport.createId(timestamp);
    }
}
