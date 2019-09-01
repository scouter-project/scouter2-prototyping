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
package scouter2.collector.domain.dict;

import org.springframework.stereotype.Component;
import scouter2.collector.domain.obj.DictRepo;
import scouter2.collector.domain.obj.Obj;
import scouter2.collector.domain.obj.ObjService;
import scouter2.proto.DictP;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-09-01
 */
@Component
public class DictAdder {

    ObjService objService;
    DictRepo repo;
    DictCacheManager cacheManager;

    public DictAdder(ObjService objService, DictRepo repo, DictCacheManager cacheManager) {
        this.objService = objService;
        this.repo = repo;
        this.cacheManager = cacheManager;
    }

    public void record(DictP dictP) {
        Obj obj = objService.findByName(dictP.getObjFullName());
        if (obj == null) {
            return;
        }

        DictCache dictCache = cacheManager.get(obj.getApplicationId());
        if (dictCache.get(dictP.getCategory(), dictP.getDictHash()) == null) {
            dictCache.put(dictP.getCategory(), dictP.getDictHash(), dictP.getText());
            repo.addOrModify(obj.getApplicationId(), dictP.getCategory(), dictP.getDictHash(), dictP.getText());
        }
    }
}
