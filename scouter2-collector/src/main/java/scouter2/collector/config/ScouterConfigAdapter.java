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

package scouter2.collector.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.common.config.ConfigItem;
import scouter2.common.config.ScouterConfigIF;
import scouter2.common.helper.Props;
import scouter2.common.util.MiscUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-18
 */
@Slf4j
public abstract class ScouterConfigAdapter implements ScouterConfigIF {

    public static final ThrottleConfig S_0034 = ThrottleConfig.of("S0034");
    public static final ThrottleConfig S_0033 = ThrottleConfig.of("S0033");
    public static final ThrottleConfig S_0032 = ThrottleConfig.of("S0032");
    List<ConfigFieldInfo> fieldInfoList = new ArrayList<>();

    @Data
    @AllArgsConstructor
    static class ConfigFieldInfo {
        Field field;
        Configurable configurable;
    }

    public ScouterConfigAdapter(boolean dummy) {

    }

    public ScouterConfigAdapter() {
        try {
            Field[] declaredFields = this.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                Configurable configurable = field.getAnnotation(Configurable.class);
                if (configurable != null) {
                    field.setAccessible(true);
                    fieldInfoList.add(new ConfigFieldInfo(field, configurable));
                }
            }
        } catch (Exception e) {
            log.error("{}, {}", this.getClass().getName(), e.getMessage(), S_0034, e);
        }
    }

    protected void refresh(ScouterConfigAdapter initValuedConfig, Props props) {
        try {
            for (ConfigFieldInfo fieldInfo : this.fieldInfoList) {
                Field field = fieldInfo.getField();

                if (field.getType() == String.class) {
                    field.set(this, props.getString(field.getName(), (String) field.get(initValuedConfig)));

                } else if (field.getType() == int.class || field.getType() == Integer.class) {
                    field.setInt(this, props.getInt(field.getName(), field.getInt(initValuedConfig)));

                } else if (field.getType() == long.class || field.getType() == Long.class) {
                    field.setLong(this, props.getLong(field.getName(), field.getLong(initValuedConfig)));

                } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                    field.setBoolean(this, props.getBoolean(field.getName(), field.getBoolean(initValuedConfig)));

                } else {
                    field.set(this, props.getString(field.getName(), (String) field.get(initValuedConfig)));
                }
            }
        } catch (Exception e) {
            log.error("{}, {}", this.getClass().getName(), e.getMessage(), S_0032, e);
        }
    }

    protected List<ConfigItem> getAllConfigs(ScouterConfigAdapter initValuedConfig) {
        List<ConfigItem> items = new ArrayList<>();

        try {
            for (ConfigFieldInfo fieldInfo : this.fieldInfoList) {
                Field field = fieldInfo.getField();
                Configurable annot = fieldInfo.getConfigurable();

                String underscoredKey = MiscUtil.camelCaseToUnderscore(field.getName());
                ConfigItem configItem = new ConfigItem(underscoredKey, String.valueOf(field.get(this)),
                        String.valueOf(field.get(initValuedConfig)), annot.value(), annot.type().getType());

                items.add(configItem);
            }
        } catch (Exception e) {
            log.error("{}, {}", this.getClass().getName(), e.getMessage(), S_0033, e);
        }

        return items;

    }
}
