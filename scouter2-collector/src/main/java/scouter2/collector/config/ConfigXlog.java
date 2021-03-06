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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import scouter2.common.config.ConfigItem;
import scouter2.common.helper.Props;

import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Getter
@Slf4j
@Component
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigXlog extends ScouterConfigAdapter {
    static ConfigXlog initValuedConfig = new ConfigXlog();

    @Configurable int xlogQueueSize = 10000;
    @Configurable int xlogProfileQueueSize = 10000;
    @Configurable int xlogReceiverThreadCount = 1;
    @Configurable int xlogRepoThreadCount = 1;
    @Configurable int xlogLoopCacheSize = 100000;

    @Configurable int xlogRepoPurgeXlogDays = 30;
    @Configurable int xlogRepoPurgeProfileDays = 15;

    @Configurable int _xlogGxidWriteBufferKeepMillis = 15000;
    @Configurable int _xlogGxidWriteBufferSize = 150000;

    @Configurable int _xlogProfileOffsetWriteBufferKeepMillis = 15000;
    @Configurable int _xlogProfileOffsetWriteBufferSize = 150000;

    @Override
    public List<ConfigItem> getAllConfigs() {
        return super.getAllConfigs(initValuedConfig);
    }

    @Override
    public void refresh(Props props) {
        super.refresh(initValuedConfig, props);
    }
}
