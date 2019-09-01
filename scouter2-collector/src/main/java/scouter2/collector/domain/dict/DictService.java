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

import org.springframework.stereotype.Service;
import scouter2.collector.domain.obj.DictRepo;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-09-01
 */
@Service
public class DictService {
    private static DictService instance;

    DictCacheManager dictCacheManager;
    DictRepo repo;


    public DictService(DictCacheManager dictCacheManager, DictRepo dictRepo) {
        synchronized (DictService.class) {
            if (instance != null) {
                throw new IllegalStateException();
            }
            this.dictCacheManager = dictCacheManager;
            this.repo = dictRepo;
            instance = this;
        }
    }

    //for 3rd party transport
    public static DictService getInstance() {
        return instance;
    }

    public String find(String applicationId, String category, int hash) {
        DictCache dictCache = dictCacheManager.get(applicationId);
        String text = dictCache.get(category, hash);
        if (text == null) {
            text = repo.find(applicationId, category, hash);
            //TODO null from repo skip timer
        }
        return text;
    }
}
