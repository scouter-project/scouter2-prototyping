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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.eclipsecollections.EclipseCollectionsModule;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
public class DefaultObjectMapper extends ObjectMapper {

    public DefaultObjectMapper() {
        init();
    }

    public DefaultObjectMapper init() {
        try {
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
                    .registerModule(new EclipseCollectionsModule())
            ;

        } catch (Exception e) {
            throw new IllegalStateException("DefaultObjectMapper init failed for following reason", e);
        }
        return this;
    }
}
