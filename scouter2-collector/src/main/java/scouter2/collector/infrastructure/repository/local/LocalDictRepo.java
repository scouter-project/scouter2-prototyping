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

package scouter2.collector.infrastructure.repository.local;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.obj.DictRepo;
import scouter2.collector.domain.obj.DictRepoAdapter;
import scouter2.collector.infrastructure.db.mapdb.DictDb;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-17
 */
@Slf4j
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("local")
public class LocalDictRepo extends DictRepoAdapter implements DictRepo, NonThreadSafeRepo {

    private DictDb db;

    public LocalDictRepo(DictDb dictDb) {
        this.db = dictDb;
    }

    @Override
    public String getRepoType() {
        return LocalRepoConstant.TYPE_NAME;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        db.closeAll();
    }


    @Override
    public void addOrModify(String applicationId, String category, int hash, String text) {
        db.add(category, hash, text);
    }

    @Override
    public String find(String applicationId, String category, int hash) {
        return db.get(category, hash);
    }
}
