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

package scouter2.collector.domain.instancetype;

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
import scouter2.common.meta.InstanceType;
import scouter2.common.meta.MetricDef;

import javax.annotation.PostConstruct;
import java.io.InputStream;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
@Component
@Slf4j
public class InstanceTypeManager {

    public static final String INSTANCE_TYPES_FILE_NAME = "/instanceTypes.json";

    private ImmutableList<InstanceType> instanceTypeList = Lists.immutable.empty();
    private ImmutableMap<String, InstanceType> instanceTypeMap = Maps.immutable.empty();

    @PostConstruct
    public void init() {
        reload();
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void schedule() {
        reload();
    }

    private void reload() {
        InputStream in  = CounterEngine.class.getResourceAsStream(INSTANCE_TYPES_FILE_NAME);
        try {
            byte[] contents = FileUtil.readAll(in);
            String jsonInstanceTypes = new String(contents);

            MutableList<InstanceType> types = JsonUtil.toObjectList(jsonInstanceTypes,
                    new TypeReference<MutableList<InstanceType>>() {});

            //TODO add custom
            instanceTypeList = types.toImmutable();

            MutableMap<String, InstanceType> byType = types.toMap(InstanceType::getId, v -> v);
            //TODO add custom
            instanceTypeMap = byType.toImmutable();

        } catch (Exception e) {
            log.error("Failed read " + INSTANCE_TYPES_FILE_NAME, e);

        } finally {
            FileUtil.close(in);
        }
    }

    public InstanceType getInstanceType(String instanceTypeId) {
        return instanceTypeMap.get(instanceTypeId);
    }

    public ImmutableList<MetricDef> getMetricDefsByInstanceType(String instanceTypeId) {
        InstanceType instanceType = instanceTypeMap.get(instanceTypeId);
        if (instanceType == null) {
            return Lists.immutable.empty();
        }

        return Lists.immutable.ofAll(instanceType.getMetricDefs());
    }
}
