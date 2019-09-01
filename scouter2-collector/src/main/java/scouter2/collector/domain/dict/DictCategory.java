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

import lombok.Getter;
import scouter.lang.TextTypes;
import scouter2.collector.config.ConfigXlog;
import scouter2.collector.config.support.ConfigManager;

import java.util.function.Supplier;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 31/08/2019
 */
@Getter
public enum DictCategory {
    ERROR("error", TextTypes.ERROR, 2000,
            () -> Integer.MAX_VALUE),
    APICALL("apicall", TextTypes.APICALL, 10000,
            DictCategory::xlogRelatedExpire),
    METHOD("method", TextTypes.METHOD, 5000,
            DictCategory::xlogRelatedExpire),
    SERVICE("service", TextTypes.SERVICE, 10000,
            DictCategory::xlogRelatedExpire),
    SQL("sql", TextTypes.SQL, 5000,
            DictCategory::xlogRelatedExpire),
    OBJ("obj", TextTypes.OBJECT, 2000,
            () -> Integer.MAX_VALUE),
    REFERRER("referrer", TextTypes.REFERER, 2000,
            DictCategory::xlogRelatedExpire),
    USER_AGENT("ua", TextTypes.USER_AGENT,10000,
            DictCategory::xlogRelatedExpire),
    GROUP("group", TextTypes.GROUP, 2000,
            DictCategory::xlogRelatedExpire),
    CITY("city", TextTypes.CITY, 2000,
            () -> Integer.MAX_VALUE),
    SQL_TABLE("table", TextTypes.SQL_TABLES, 2000,
            () -> Integer.MAX_VALUE),
    LOGIN("tagLogin", TextTypes.LOGIN, 5000,
            DictCategory::xlogRelatedExpire),
    DESC("tagDesc", TextTypes.DESC, 5000,
            DictCategory::xlogRelatedExpire),
    WEB("web", TextTypes.WEB, 5000,
            DictCategory::xlogRelatedExpire),
    HASH_MSG("hmsg", TextTypes.HASH_MSG, 5000,
            DictCategory::xlogRelatedExpire),
    STACK_ELEMENT("stackElm", TextTypes.STACK_ELEMENT, 5000,
            DictCategory::xlogRelatedExpire),
    UNDEFINED("undef_%s", null, 5000,
            () -> Integer.MAX_VALUE);

    String category;
    String legacyCategory;
    int cacheHint;
    Supplier<Integer> expireDaySupplier;

    DictCategory(String category, String legacyCategory, int cacheHint, Supplier<Integer> expireDaySupplier) {
        this.category = category;
        this.legacyCategory = legacyCategory;
        this.cacheHint = cacheHint;
        this.expireDaySupplier = expireDaySupplier;
    }

    private static int xlogRelatedExpire() {
        //TODO adjust to tag count duration
        ConfigXlog configXlog = ConfigManager.getConfig(ConfigXlog.class);
        return 2 * configXlog.getXlogRepoPurgeXlogDays();
    }

    public static DictCategory of(String category) {
        for (DictCategory dc : DictCategory.values()) {
            if (dc.category.equals(category)) {
                return dc;
            }
        }
        return UNDEFINED;
    }

    public static DictCategory ofLegacy(String xtype) {
        for (DictCategory dc : DictCategory.values()) {
            if (dc.legacyCategory.equals(xtype)) {
                return dc;
            }
        }
        return UNDEFINED;
    }

    public String genUndefinedCategoryName(String category) {
        if (this != UNDEFINED) {
            throw new IllegalStateException("Not undefined dict category.");
        }
        return String.format(this.category, category);
    }

    public int getExpireDaysAfterLastTouch() {
        return expireDaySupplier.get();
    }
}
