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

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
public class MetricDef {
    private String id;
    private String displayName;
    private MetricType metricType;
    private Unit unit;
    private String icon4Legacy;
    private boolean displayTotal;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getIcon4Legacy() {
        return icon4Legacy;
    }

    public void setIcon4Legacy(String icon4Legacy) {
        this.icon4Legacy = icon4Legacy;
    }

    public boolean isDisplayTotal() {
        return displayTotal;
    }

    public void setDisplayTotal(boolean displayTotal) {
        this.displayTotal = displayTotal;
    }

    @Override
    public String toString() {
        return "MetricDef{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", metricType=" + metricType +
                ", unit=" + unit +
                ", icon4Legacy='" + icon4Legacy + '\'' +
                ", displayTotal=" + displayTotal +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetricDef metric = (MetricDef) o;

        return id != null ? id.equals(metric.id) : metric.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
