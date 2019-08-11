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

import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
public class ObjType {
    private String id;
    private String masterMetricName;
    private List<MetricDef> metricDefs;

    public static ObjType empty() {
        return new ObjType();
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

    public void setMetricDefs(List<MetricDef> metricDefs) {
        this.metricDefs = metricDefs;
    }

    @Override
    public String toString() {
        return "ObjType{" +
                "id='" + id + '\'' +
                ", masterMetric='" + masterMetricName + '\'' +
                ", metrics=" + metricDefs +
                '}';
    }
}
