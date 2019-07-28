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

package scouter2.collector.springconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.domain.instance.InstanceRepo;
import scouter2.collector.domain.xlog.XlogRepo;
import scouter2.collector.infrastructure.repository.mute.MutingInstanceRepo;
import scouter2.collector.infrastructure.repository.mute.MutingXlogRepo;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-28
 */
@Configuration
public class RepoConfig {

    @Bean
    public InstanceRepo instanceRepo(RepoSelector repoSelector, ConfigCommon configCommon) {
        InstanceRepo repo = repoSelector.findRepoByType(configCommon.getRepoType(), InstanceRepo.class);
        if (repo != null) {
            return repo;
        } else {
            return new MutingInstanceRepo();
        }
    }

    @Bean
    public XlogRepo xlogRepo(RepoSelector repoSelector, ConfigCommon configCommon) {
        XlogRepo repo = repoSelector.findRepoByType(configCommon.getRepoType(), XlogRepo.class);
        if (repo != null) {
            return repo;
        } else {
            return new MutingXlogRepo();
        }
    }

}
