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

package scouter2.collector.springconfig;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import scouter2.collector.CollectorApplication;
import scouter2.common.helper.Props;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-30
 */
public class RepoTypeSelectorCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String repoType = (String) metadata
                .getAnnotationAttributes("scouter2.collector.springconfig.RepoTypeMatch")
                .get("value");

        Props loadProps = CollectorApplication.getLoadProps();
        if (repoType.equals(loadProps.getString("repoType"))) {
            return true;
        }

        return false;
    }
}
