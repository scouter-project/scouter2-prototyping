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

package scouter2.collector.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import scouter2.collector.common.DefaultObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
@Slf4j
public class JsonUtil {

    private static final DefaultObjectMapper objectMapper;

    static {
        objectMapper = new DefaultObjectMapper();
    }

    public static String toString(Object object) {

        if (object == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new IllegalStateException(e.toString(), e);
        }
    }

    public static <T> T toObject(String json, Class<T> clazz) {

        try {
            return StringUtils.isEmpty(json) ? null : objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new IllegalStateException(e.toString(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T toObject(String json, TypeReference<T> typeReference) {

        try {
            return (T) (StringUtils.isEmpty(json) ? null : objectMapper.readValue(json, typeReference));
        } catch (Exception e) {
            throw new IllegalStateException(e.toString(), e);
        }
    }

    public static Map<String, Object> toMap(String json) {
        return toObject(json, new TypeReference<Map<String, Object>>() {});
    }

    public static Map<String, Object> toMapWithoutException(String json) {
        if (json == null) {
            return null;
        }

        Map<String, Object> stringObjectMap = null;
        try {
            stringObjectMap = toObject(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("[ERROR] JsonUtil.toMapWithoutException(), msg: {}, json: {}", e.getMessage(), json);
        }
        return stringObjectMap;
    }

    @SuppressWarnings("unchecked")
    public static <T> T toObjectList(String jsonString, TypeReference<T> typeReference) {

        try {
            if (jsonString == null) return null;
            return (T) objectMapper.readValue(jsonString, typeReference);
        } catch (Exception e) {
            throw new IllegalStateException(e.toString(), e);
        }
    }

    public static List<String> toStringList(String jsonString) {

        try {
            if (jsonString == null) return null;
            return objectMapper.readValue(jsonString, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new IllegalStateException(e.toString(), e);
        }
    }
}
