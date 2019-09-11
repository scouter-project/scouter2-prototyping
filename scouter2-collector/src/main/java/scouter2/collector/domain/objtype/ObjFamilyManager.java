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
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.common.util.JsonUtil;
import scouter2.collector.springconfig.ThreadNameDecorator;
import scouter2.common.meta.MetricDef;
import scouter2.common.meta.ObjFamily;

import javax.annotation.PostConstruct;
import java.io.InputStream;

import static scouter2.common.meta.MetricConstant.OBJ_FAMILIES_FILE_NAME;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
@Component
@Slf4j
public class ObjFamilyManager {

    public static final ThrottleConfig S_0012 = ThrottleConfig.of("S0012");

    private static ObjFamilyManager instance;

    private ImmutableList<ObjFamily> objFamilyList = Lists.immutable.empty();
    private ImmutableMap<String, ObjFamily> objFamilyMap = Maps.immutable.empty();

    public ObjFamilyManager() {
        synchronized (ObjFamilyManager.class) {
            if (instance != null) {
                throw new IllegalStateException();
            }
            instance = this;
        }
    }

    public static ObjFamilyManager getInstance() {
        return instance;
    }

    @PostConstruct
    public void init() {
        reload();
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void schedule() {
        ThreadNameDecorator.runWithName(this.getClass().getSimpleName(), () -> {
            reload();
        });
    }

    private void reload() {
        InputStream in = CounterEngine.class.getResourceAsStream(OBJ_FAMILIES_FILE_NAME);
        try {
            byte[] contents = FileUtil.readAll(in);
            String jsonObjTypes = new String(contents);

            MutableList<ObjFamily> families = JsonUtil.toObjectList(jsonObjTypes,
                    new TypeReference<MutableList<ObjFamily>>() {
                    });

            //TODO add custom
            objFamilyList = families.toImmutable();

            MutableMap<String, ObjFamily> byFamily = families.toMap(ObjFamily::getId, v -> v);
            //TODO add custom
            objFamilyMap = byFamily.toImmutable();

        } catch (Exception e) {
            log.error("Failed read " + OBJ_FAMILIES_FILE_NAME, S_0012, e);

        } finally {
            FileUtil.close(in);
        }
    }

    public ObjFamily getObjFamily(String familyId) {
        return objFamilyMap.get(familyId);
    }

    public ImmutableList<ObjFamily> getAll() {
        return objFamilyList;
    }

    public ImmutableList<MetricDef> getMetricDefs(String familyId) {
        ObjFamily objFamily = objFamilyMap.get(familyId);
        if (objFamily == null) {
            return Lists.immutable.empty();
        }

        return Lists.immutable.ofAll(objFamily.getMetricDefs());
    }

    public String getMasterMetricName(String familyId) {
        ObjFamily objFamily = objFamilyMap.get(familyId);
        if (objFamily == null) {
            return null;
        }
        return objFamily.getMasterMetricName();
    }
}
