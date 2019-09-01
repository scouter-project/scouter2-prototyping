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

package scouter2.collector.infrastructure.db.mapdb;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.config.ConfigXlog;
import scouter2.collector.domain.dict.DictCategory;
import scouter2.collector.main.CoreRun;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
@Getter
@Slf4j
@Component
@RepoTypeMatch("local")
@Conditional(RepoTypeSelectorCondition.class)
public class DictDb {

    private static final long AUTO_CLOSE_MILLIS = 120 * 1000;
    public static final String INDEX_NAME = "dict";

    public static final ThrottleConfig S_0051 = ThrottleConfig.of("S0051");

    private ConfigCommon configCommon;
    private ConfigMapDb configMapDb;
    private ConfigXlog configXlog;

    private final ConcurrentHashMap<String, CategoryDictDb> CategoryDictDbMap = new ConcurrentHashMap<>();

    public DictDb(ConfigCommon configCommon, ConfigMapDb configMapDb, ConfigXlog configXlog) {
        this.configCommon = configCommon;
        this.configMapDb = configMapDb;
        this.configXlog = configXlog;

        for (DictCategory category : DictCategory.values()) {
            if (category != DictCategory.UNDEFINED) {
                defineDb(category.getCategory(), category.getExpireDaysAfterLastTouch());
            }
        }
    }

    public void closeAll() {
        synchronized (CategoryDictDbMap) {
            for (Map.Entry<String, CategoryDictDb> e : CategoryDictDbMap.entrySet()) {
                try {
                    e.getValue().close();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), e);
                }
            }
        }
    }

    @Scheduled(fixedDelay = 500, initialDelay = 1000)
    public void schedule4Commit() {
        for (CategoryDictDb value : CategoryDictDbMap.values()) {
            if (CoreRun.isRunning() && !value.getDb().isClosed()) {
                value.getDb().commit();
            }
        }
    }

    public void add(String category, int hash, String text) {
        getOfCategory(category).getDictMap().put(hash, text);
    }

    public String get(String category, int hash) {
        return getOfCategory(category).getDictMap().get(hash);
    }

    private CategoryDictDb getOfCategory(String category) {
        CategoryDictDb categoryDictDb = CategoryDictDbMap.get(category);
        if (categoryDictDb != null) {
            return categoryDictDb;
        }
        DictCategory dc = DictCategory.of(category);
        if (dc == DictCategory.UNDEFINED) {
            categoryDictDb = defineDb(dc.genUndefinedCategoryName(category), dc.getExpireDaysAfterLastTouch());
        } else {
            categoryDictDb = defineDb(category, dc.getExpireDaysAfterLastTouch());
        }
        return categoryDictDb;
    }

    private CategoryDictDb defineDb(String category, int expireDaysAfterLastTouch) {
        synchronized (CategoryDictDbMap) {
            if (CategoryDictDbMap.get(category) != null) {
                return CategoryDictDbMap.get(category);
            }
            CategoryDictDb categoryDictDb = new CategoryDictDb(category, expireDaysAfterLastTouch, configCommon, configMapDb);
            CategoryDictDbMap.put(category, categoryDictDb);
            return categoryDictDb;
        }
    }
}
