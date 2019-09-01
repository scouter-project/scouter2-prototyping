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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.springframework.stereotype.Service;
import scouter.util.HashUtil;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigObj;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-04
 */
@Service
public class ObjService {
    private static ObjService instance;

    ObjRepo repo;
    ObjServiceCache cache;
    ConfigObj configObj;

    public ObjService(ObjRepo repo, ObjServiceCache cache, ConfigObj configObj) {
        synchronized (ObjService.class) {
            if (instance != null) {
                throw new IllegalStateException();
            }
            this.repo = repo;
            this.cache = cache;
            this.configObj = configObj;
            instance = this;
        }
    }

    //for 3rd party transport
    public static ObjService getInstance() {
        return instance;
    }

    public Long findIdByName(String objFullName) {
        Long objID = findIdByName0(objFullName);
        if (objID == null) {
            //For scouter agent1 -> agent2 upgrade
            objID = findIdByName0(String.valueOf(HashUtil.hash(objFullName)));
        }
        return objID;
    }

    public Obj findByName(String objFullName) {
        Long objID = findIdByName0(objFullName);
        if (objID == null) {
            return null;
        }
        return findById(objID);
    }

    private static boolean isScouterLegacyObjHash(String objFullName) {
        return StringUtils.isNumeric(StringUtils.remove(objFullName, '-'));
    }

    private Long findIdByName0(String objFullName) {
        return cache.findIdByName(objFullName);
    }

    public Obj findById(long objId) {
        return cache.findById(objId);
    }

    public long generateUniqueIdByName(String fullNameOrLegacyHash) {
        return repo.generateUniqueIdByName(fullNameOrLegacyHash);
    }

    public MutableList<Obj> findByApplicationId(String applicationId) {
        return cache.findByApplicationId(applicationId);
    }

    public MutableList<Obj> findByLegacyObjType(String legacyObjType) {
        return cache.findByLegacyObjType(legacyObjType);
    }

    public MutableList<Obj> findAll() {
        return Lists.adapt(repo.findAll());
    }

    public boolean isDeadObject(Obj obj) {
        return obj.isDead(configObj.getObjDeadTime(), U.now());
    }
}
