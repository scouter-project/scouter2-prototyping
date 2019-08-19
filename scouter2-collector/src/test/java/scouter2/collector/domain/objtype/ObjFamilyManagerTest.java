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

package scouter2.collector.domain.objtype;

import org.eclipse.collections.api.list.ImmutableList;
import org.junit.Test;
import scouter2.common.meta.MetricDef;
import scouter2.common.meta.ObjFamily;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-18
 */
public class ObjFamilyManagerTest {

    @Test
    public void initTest() {
        ObjFamilyManager objFamilyManager = new ObjFamilyManager();
        objFamilyManager.init();

        ImmutableList<ObjFamily> all = objFamilyManager.getAll();
        ObjFamily actual0 = all.get(0);
        List<MetricDef> metricDefs = actual0.getMetricDefs();
        MetricDef metric0 = metricDefs.get(0);

        assertThat(all.size()).isGreaterThan(0);
        assertThat(actual0.getId()).isNotNull();
        assertThat(actual0.getMasterMetricName()).isNotNull();

        assertThat(metricDefs.size()).isGreaterThan(0);
        assertThat(metric0.getId()).isNotNull();
        assertThat(metric0.getDisplayName()).isNotNull();
        assertThat(metric0.getUnit()).isNotNull();
    }
}