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

package scouter2.collector.domain.obj;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-04
 */
@Service
public class ObjServiceCache {
    ObjRepo repo;

    public ObjServiceCache(ObjRepo repo) {
        this.repo = repo;
    }

    @Cacheable(cacheNames = "objId", sync = true)
    public Long findIdByName(String objFullName) {
        return repo.findIdByName(objFullName);
    }

    @Cacheable(cacheNames = "obj")
    public Obj findById(long objId) {
        return repo.findById(objId);
    }

    @Cacheable(cacheNames = "objList")
    public MutableList<Obj> findByApplicationId(String applicationId) {
        return Lists.mutable.withAll(repo.findByApplicationId(applicationId));
    }

    @Cacheable(cacheNames = "objList")
    public MutableList<Obj> findByLegacyObjType(String legacyObjType) {
        return Lists.mutable.withAll(repo.findByLegacyObjType(legacyObjType));
    }
}
