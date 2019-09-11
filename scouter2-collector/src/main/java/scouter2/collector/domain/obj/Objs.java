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

package scouter2.collector.domain.obj;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-09-11
 */
@Slf4j
public class Objs {

    public static final String DELIM = "::";

    public static ObjTypeInfo parseObjTypeParam(String objTypeParam) {
        int delim = objTypeParam.lastIndexOf(DELIM);
        if (delim > -1) {
            return ObjTypeInfo.builder()
                    .isLegacyObjType(false)
                    .applicationId(objTypeParam.substring(0, delim))
                    .family(objTypeParam.substring(delim + DELIM.length()))
                    .build();
        } else {
            return ObjTypeInfo.builder()
                    .isLegacyObjType(true)
                    .legacyObjType(objTypeParam)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ObjTypeInfo {
        boolean isLegacyObjType;
        String applicationId;
        String family;
        String legacyObjType;
    }
}
