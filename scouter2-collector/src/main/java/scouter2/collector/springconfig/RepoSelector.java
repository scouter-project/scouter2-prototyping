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

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.multimap.list.MutableListMultimap;
import org.eclipse.collections.impl.factory.Multimaps;
import org.springframework.stereotype.Service;
import scouter2.collector.domain.ScouterRepo;
import scouter2.collector.main.Scanner;
import scouter2.collector.plugin.Scouter2PluginMeta;

import javax.annotation.PostConstruct;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-28
 */
@Slf4j
@Service
public class RepoSelector {
    private MutableListMultimap<String, ScouterRepo> repoTypeMap = Multimaps.mutable.list.empty();

    @PostConstruct
    public void init() {
        addScouterRepos();
        add3rdPartyRepos();
    }

    private void addScouterRepos() {
        Set<String> classes = new Scanner("scouter2.collector.infrastructure.repository").process();
        addRepos0(classes);
    }

    private void add3rdPartyRepos() {
        ServiceLoader<Scouter2PluginMeta> loader = ServiceLoader.load(Scouter2PluginMeta.class);
        for (Scouter2PluginMeta meta : loader) {
            Set<String> classes = new Scanner(meta.getComponentScanPackage()).process();
            addRepos0(classes);
        }
    }

    private void addRepos0(Set<String> classes) {
        for (String aClass : classes) {
            try {
                Class<?> clazz = Class.forName(aClass);
                if (!ScouterRepo.class.isAssignableFrom(clazz)) {
                    continue;
                }
                if (ScouterRepo.class == clazz) {
                    continue;
                }
                ScouterRepo repo = (ScouterRepo) clazz.newInstance();
                repoTypeMap.put(repo.getRepoType(), repo);

            } catch (Exception e) {
                log.error("Exception on loading scouter repo classes", e);
            }
        }
    }

    public <T extends ScouterRepo> T findRepoByType(String type, Class<T> clazz) {
        MutableList<ScouterRepo> repos = repoTypeMap.get(type);
        ScouterRepo found = repos.select(repo -> clazz.isAssignableFrom(repo.getClass())).getFirst();

        return (T) found;
    }
}
