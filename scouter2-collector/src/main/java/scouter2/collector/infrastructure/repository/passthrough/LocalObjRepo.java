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

import lombok.extern.slf4j.Slf4j;
import org.mapdb.Atomic;
import org.mapdb.HTreeMap;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjRepo;
import scouter2.collector.domain.obj.ObjRepoAdapter;
import scouter2.collector.infrastructure.db.mapdb.ObjDb;
import scouter2.collector.infrastructure.repository.local.LocalRepoConstant;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.collection.LruMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-17
 */
@Slf4j
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("test")
public class LocalObjRepo extends ObjRepoAdapter implements ObjRepo, NonThreadSafeRepo {

    private ObjDb db;
    private HTreeMap<Long, Obj> objMap;
    private HTreeMap<String, Long> objNameIdMap;
    private Atomic.Long objIdGenerator;

    private Map<Long, Obj> objCache = LruMap.newOfMax(2000);
    private Map<String, Long> objNameIdCache = LruMap.newOfMax(2000);

    public LocalObjRepo(ObjDb objDb) {
        this.db = objDb;
        objMap = objDb.getObjMap();
        objNameIdMap = objDb.getObjNameIdMap();
        objIdGenerator = objDb.getObjIdGenerator();

        for (Obj obj : findAllFromPersistence()) {
            objCache.put(obj.getObjId(), obj);
            objNameIdCache.put(obj.getObjFullName(), obj.getObjId());
        }
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
        db.close();
    }


    @Override
    public void addOrModify(Obj obj) {
        boolean renew = true;
        Obj cached = objCache.get(obj.getObjId());
        if (cached != null) {
            cached.setLastActive(obj.getLastActive());
            renew = !cached.equals(obj);
        }

        if (renew) {
            objMap.put(obj.getObjId(), obj);
            objNameIdMap.put(obj.getFullNameOrLegacyHash(), obj.getObjId());
            db.commit();

            objCache.put(obj.getObjId(), obj);
            objNameIdCache.put(obj.getFullNameOrLegacyHash(), obj.getObjId());
        }
    }

    @Override
    public void remove(long objId) {
        objMap.remove(objId);
        objCache.remove(objId);
    }

    @Override
    public Long findIdByName(String objFullName) {
        Long objId = objNameIdCache.get(objFullName);
        if (objId == null) {
            objId =  objNameIdMap.get(objFullName);
            if (objId != null) {
                objNameIdCache.put(objFullName, objId);
            }
        }
        return objId;
    }

    @Override
    public Long generateUniqueIdByName(String fullNameOrLegacyHash) {
        Long id = objNameIdMap.get(fullNameOrLegacyHash);
        if (id == null) {
            long newId = objIdGenerator.incrementAndGet();
            db.commit();
            //There can exist something of legacy objHash.
            if (objMap.containsKey(newId)) {
                return generateUniqueIdByName(fullNameOrLegacyHash);
            }
            objNameIdMap.put(fullNameOrLegacyHash, newId);
            objNameIdCache.put(fullNameOrLegacyHash, newId);
            db.commit();
            return newId;

        } else {
            return id;
        }
    }

    @Override
    public Obj findById(long objId) {
        Obj obj = objCache.get(objId);
        if (obj == null) {
            obj = objMap.get(objId);
            if (obj != null) {
                objCache.put(objId, obj);
            }
        }
        return obj;
    }

    @Override
    public List<Obj> findByApplicationId(String applicationId) {
        return objCache.values().stream()
                .filter(obj -> !obj.isDeleted())
                .filter(obj -> applicationId.equals(obj.getApplicationId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Obj> findByLegacyObjType(String legacyObjType) {
        return objCache.values().stream()
                .filter(obj -> !obj.isDeleted())
                .filter(obj -> legacyObjType.equals(obj.getObjLegacyType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Obj> findAll() {
        return objCache.values().stream()
                .filter(obj -> !obj.isDeleted())
                .collect(Collectors.toList());
    }

    protected List<Obj> findAllFromPersistence() {
        return objMap.values().stream()
                .filter(obj -> !obj.isDeleted())
                .collect(Collectors.toList());
    }

    public void commit() {
        if (!db.getDb().isClosed()) {
            db.getDb().commit();
        }
    }
}