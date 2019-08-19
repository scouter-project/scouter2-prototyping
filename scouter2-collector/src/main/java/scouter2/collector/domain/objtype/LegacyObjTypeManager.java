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

package scouter2.collector.domain.objtype;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.springframework.stereotype.Component;
import scouter.lang.counters.CounterEngine;
import scouter.util.FileUtil;
import scouter2.collector.common.util.JsonUtil;
import scouter2.common.meta.LegacyType2Family;

import java.io.IOException;
import java.io.InputStream;

import static scouter2.common.meta.MetricConstant.OBJ_FAMILY_LEGACY_MAPPING_FILE_NAME;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-18
 */
@Component
@Slf4j
public class LegacyObjTypeManager {

    private static LegacyObjTypeManager instance;

    ImmutableMap<String, LegacyType2Family> legacyType2FamilyMappings;

    public LegacyObjTypeManager() throws IOException {
        synchronized (LegacyObjTypeManager.class) {
            if (instance != null) {
                throw new IllegalStateException();
            }
            instance = this;
            init();
        }
    }

    public static LegacyObjTypeManager getInstance() {
        return instance;
    }

    public void init() throws IOException {
        InputStream in  = CounterEngine.class.getResourceAsStream(OBJ_FAMILY_LEGACY_MAPPING_FILE_NAME);
        try {
            byte[] contents = FileUtil.readAll(in);
            String jsonLegacy2FamilyMappings = new String(contents);

            MutableList<LegacyType2Family> mappings = JsonUtil.toObjectList(jsonLegacy2FamilyMappings,
                    new TypeReference<MutableList<LegacyType2Family>>() {});

            legacyType2FamilyMappings = mappings.toMap(LegacyType2Family::getId, v -> v).toImmutable();

        } finally {
            FileUtil.close(in);
        }
    }

    public LegacyType2Family getMapping(String legacyObjType) {
        return legacyType2FamilyMappings.get(legacyObjType);
    }

    public ImmutableList<LegacyType2Family> getAll() {
        return legacyType2FamilyMappings.toList().toImmutable();
    }
}
