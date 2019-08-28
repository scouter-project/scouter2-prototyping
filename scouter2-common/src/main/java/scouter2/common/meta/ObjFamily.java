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

package scouter2.common.meta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
public class ObjFamily {
    private String id;
    private String masterMetricName;
    private List<MetricDef> metricDefs;
    private Map<String, MetricDef> metricDefMap;

    public static ObjFamily empty() {
        return new ObjFamily();
    }

    public String getId() {
        return id;
    }

    public boolean isEmpty() {
        return id == null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMasterMetricName() {
        return masterMetricName;
    }

    public void setMasterMetricName(String masterMetricName) {
        this.masterMetricName = masterMetricName;
    }

    public List<MetricDef> getMetricDefs() {
        return metricDefs;
    }

    public MetricDef getMasterMetricDefs() {
        return metricDefMap.get(getMasterMetricName());
    }

    public void setMetricDefs(List<MetricDef> metricDefs) {
        this.metricDefs = metricDefs;
        Map<String, MetricDef> metricDefMap = new HashMap<String, MetricDef>();
        for (MetricDef def : metricDefs) {
            metricDefMap.put(def.getId(), def);
        }
        this.metricDefMap = metricDefMap;
    }

    @Override
    public String toString() {
        return "ObjFamily{" +
                "id='" + id + '\'' +
                ", masterMetricName='" + masterMetricName + '\'' +
                ", metricDefs=" + metricDefs +
                '}';
    }
}
