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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import scouter2.collector.main.CollectorConstants;
import scouter2.common.config.ConfigItem;
import scouter2.common.config.ScouterConfigIF;
import scouter2.common.helper.Props;

import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-07
 */
@Getter
@Slf4j
@Component
public class ConfigCommon implements ScouterConfigIF {

    int netTcpPort = 6200;
    String repoType = "local";

    String dbDir = CollectorConstants.DEFAULT_DB_DIR;

    @Override
    public List<ConfigItem> getAllConfigs() {
        return null;
    }

    @Override
    public void refresh(Props props) {
        netTcpPort = props.getInt("netTcpPort", netTcpPort);
        repoType = props.getString("repoType", repoType);
        dbDir = props.getString("dbDir", dbDir);
    }
}
