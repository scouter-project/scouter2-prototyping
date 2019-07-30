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

package scouter2.collector.infrastructure.mapdb;

import lombok.Getter;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.config.support.ConfigManager;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
@Component
@RepoTypeMatch("local")
@Conditional(RepoTypeSelectorCondition.class)
public class MapDb {

    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;
    private DB db;
    private HTreeMap<String, String> commonProps;

    public void init() {
        configCommon = ConfigManager.getConfig(ConfigCommon.class);
        configMapDb = ConfigManager.getConfig(ConfigMapDb.class);

        db = DBMaker.fileDB(configCommon.getDbDir() + "sysProps.db")
                .fileChannelEnable()
                .fileMmapEnableIfSupported()
                .closeOnJvmShutdown()
                .transactionEnable()
                .make();

        commonProps = db.hashMap(configMapDb.getCommonPropsStoreName())
                .keySerializer(Serializer.STRING)
                .valueSerializer(Serializer.STRING)
                .createOrOpen();

    }
}
