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

package scouter2.common.helper;

import org.apache.commons.lang3.StringUtils;
import scouter2.common.util.MiscUtil;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
public class Props {
    Properties properties;

    public Props(Properties properties) {
        this.properties = properties;
    }

    public String getString(String key) {
        String value = StringUtils.trim(properties.getProperty(key));
        if (StringUtils.isBlank(value)) {
            String underscoredKey = MiscUtil.camelCaseToUnderscore(key);
            value = StringUtils.trim(properties.getProperty(underscoredKey));
        }
        return value;
    }

    public String getString(String key, String def) {
        return StringUtils.trim(properties.getProperty(key, def));
    }

    public int getInt(String key, int def) {
        try {
            String v = getString(key);
            if (v != null)
                return Integer.parseInt(v);
        } catch (Exception e) {
        }
        return def;
    }

    public long getLong(String key, long def) {
        try {
            String v = getString(key);
            if (v != null)
                return Long.parseLong(v);
        } catch (Exception e) {
        }
        return def;
    }

    public boolean getBoolean(String key, boolean def) {
        try {
            String v = getString(key);
            if (v != null)
                return Boolean.parseBoolean(v);
        } catch (Exception e) {
        }
        return def;
    }

    public Set<String> getStringSet(String key, String deli) {
        Set<String> set = new HashSet<String>();
        String v = getString(key);
        if (v != null) {
            String[] split = StringUtils.splitByWholeSeparatorPreserveAllTokens(v, deli);
            for (String token : split) {
                token = StringUtils.trim(token);
                if (token.length() > 0)
                    set.add(token);
            }
        }
        return set;
    }
}
