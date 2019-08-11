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
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scouter.lang.counters.CounterEngine;
import scouter.util.FileUtil;
import scouter2.collector.common.util.JsonUtil;
import scouter2.common.meta.ObjType;
import scouter2.common.meta.MetricDef;

import javax.annotation.PostConstruct;
import java.io.InputStream;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
@Component
@Slf4j
public class ObjTypeManager {

    public static final String OBJ_TYPES_FILE_NAME = "/objTypes.json";

    private ImmutableList<ObjType> objTypeList = Lists.immutable.empty();
    private ImmutableMap<String, ObjType> objTypeMap = Maps.immutable.empty();

    @PostConstruct
    public void init() {
        reload();
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void schedule() {
        reload();
    }

    private void reload() {
        InputStream in  = CounterEngine.class.getResourceAsStream(OBJ_TYPES_FILE_NAME);
        try {
            byte[] contents = FileUtil.readAll(in);
            String jsonObjTypes = new String(contents);

            MutableList<ObjType> types = JsonUtil.toObjectList(jsonObjTypes,
                    new TypeReference<MutableList<ObjType>>() {});

            //TODO add custom
            objTypeList = types.toImmutable();

            MutableMap<String, ObjType> byType = types.toMap(ObjType::getId, v -> v);
            //TODO add custom
            objTypeMap = byType.toImmutable();

        } catch (Exception e) {
            log.error("Failed read " + OBJ_TYPES_FILE_NAME, e);

        } finally {
            FileUtil.close(in);
        }
    }

    public ObjType getObjType(String objTypeId) {
        return objTypeMap.get(objTypeId);
    }

    public ImmutableList<MetricDef> getMetricDefsByObjType(String objTypeId) {
        ObjType objType = objTypeMap.get(objTypeId);
        if (objType == null) {
            return Lists.immutable.empty();
        }

        return Lists.immutable.ofAll(objType.getMetricDefs());
    }
}
