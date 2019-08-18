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

import scouter2.collector.domain.ScouterRepo;

import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public interface ObjRepo extends ScouterRepo {
    /**
     * persist obj
     * @param obj
     */
    void add(Obj obj);

    /**
     * find object id by object full name
     * @param objFullName
     * @return
     */
    Long findIdByName(String objFullName);

    /**
     * generate (unique) object id of the object full name if not exists.
     *  - less value is better for serialization.
     * if exists return it.
     * @param objFullName
     * @return
     */
    Long generateUniqueIdByName(String objFullName);

    /**
     * find object by id
     * @param objId
     * @return
     */
    Obj findById(long objId);


    void remove(long objId);

    List<Obj> findByApplicationId(String applicationId);

    List<Obj> findAll();

}
