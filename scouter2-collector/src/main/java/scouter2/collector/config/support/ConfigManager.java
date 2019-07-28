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
package scouter2.collector.config.support;

import org.springframework.stereotype.Component;
import scouter2.common.config.ScouterConfigIF;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
@Component
public class ConfigManager {

    private List<ScouterConfigIF> configs;
    private static Map<Class, ScouterConfigIF> configMap;

    public ConfigManager(List<ScouterConfigIF> configs) {
        this.configs = configs;
        configMap = configs.stream().collect(Collectors.toMap(conf -> conf.getClass(), conf -> conf, (c1, c2) -> c1));
    }

    public static <T> T getConfig(Class<T> clazz) {
        return (T) configMap.get(clazz);
    }

    public void refresh(Properties properties) {
        configs.forEach(conf -> {
            conf.refresh(properties);
        });
    }

    public void register(ScouterConfigIF configIF) {
        if (!configs.contains(configIF)) {
            configs.add(configIF);
            configMap.put(configIF.getClass(), configIF);
        }
    }
}
