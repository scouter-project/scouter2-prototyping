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

package scouter2.collector.infrastructure.repository.passthrough;

import org.eclipse.collections.impl.factory.Lists;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjRepo;
import scouter2.collector.domain.obj.ObjRepoAdapter;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;

import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 09/09/2019
 */
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("none")
public class NoneObjRepo extends ObjRepoAdapter implements ObjRepo, NonThreadSafeRepo {
    @Override
    public String getRepoType() {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void addOrModify(Obj obj) {
    }

    @Override
    public Long findIdByName(String objFullName) {
        return 0L;
    }

    @Override
    public Long generateUniqueIdByName(String objFullName) {
        return 0L;
    }

    @Override
    public Obj findById(long objId) {
        return null;
    }

    @Override
    public void remove(long objId) {
    }

    @Override
    public List<Obj> findByApplicationId(String applicationId) {
        return Lists.mutable.empty();
    }

    @Override
    public List<Obj> findByLegacyObjType(String legacyObjType) {
        return Lists.mutable.empty();
    }

    @Override
    public List<Obj> findAll() {
        return Lists.mutable.empty();
    }
}
