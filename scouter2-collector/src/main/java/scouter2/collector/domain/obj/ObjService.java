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

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-04
 */
@Service
public class ObjService {
    ObjRepo repo;
    ObjServiceCache cache;

    public ObjService(ObjRepo repo, ObjServiceCache cache) {
        this.repo = repo;
        this.cache = cache;
    }

    public Long findIdByName(String objFullName) {
        return cache.findIdByName(objFullName);
    }

    public Obj findById(long objId) {
        return cache.findById(objId);
    }

    public long generateUniqueIdByName(String fullNameOrLegacyHash) {
        return repo.generateUniqueIdByName(fullNameOrLegacyHash);
    }

    public List<Obj> findByApplicationId(String applicationId) {
        return repo.findByApplicationId(applicationId);
    }

    public List<Obj> findAll() {
        return repo.findAll();
    }
}
