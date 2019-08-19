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

package scouter2.collector.transport.legacy.service.handle;

import lombok.Getter;
import scouter2.common.meta.LegacyType2Family;
import scouter2.common.meta.MetricDef;
import scouter2.common.meta.ObjFamily;

import java.util.List;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-18
 */
@Getter
public class LegacyCountersXmlBuilder {
    FamiliesBuilder familiesBuilder = new FamiliesBuilder();
    TypesBuilder typesBuilder = new TypesBuilder();

    StringBuilder mainBuilder = new StringBuilder(prefix);

    private static final String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Counters>\n";
    private static final String postfix = "</Counters>";

    public String build() {
        return mainBuilder
                .append(familiesBuilder.build())
                .append(typesBuilder.build())
                .append(postfix).toString();
    }

    public static class FamiliesBuilder {
        private static final String prefix = "\t<Familys>\n";
        private static final String postfix = "\t</Familys>\n";
        private static final String familyPrefix = "\t\t<Family name=\"%s\" master=\"%s\">\n";
        private static final String familyPostfix = "\t\t</Family>\n";
        private static final String counterFormat = "\t\t\t<Counter name=\"%s\" disp=\"%s\" unit=\"%s\" icon=\"%s\" total=\"%s\" />\n";

        StringBuilder builder = new StringBuilder(prefix);

        public void append(ObjFamily objFamily) {
            builder.append(String.format(familyPrefix, objFamily.getId(), objFamily.getMasterMetricName()));
            if (objFamily.getMetricDefs() != null) {
                appendCounters(objFamily.getMetricDefs());
            }
            builder.append(familyPostfix);
        }

        private void appendCounters(List<MetricDef> metricDefs) {
            for (MetricDef metric : metricDefs) {
                builder.append(String.format(counterFormat, metric.getId(), metric.getDisplayName(),
                        metric.getUnit().getDisplay(), metric.getIcon4Legacy(), metric.isDisplayTotal()));
            }
        }

        protected String build() {
            return builder.append(postfix).toString();
        }
    }

    public static class TypesBuilder {
        private static final String prefix = "\t<Types>\n";
        private static final String postfix = "\t</Types>\n";
        private static final String itemFormat = "\t\t<ObjectType name=\"%s\" family=\"%s\" disp=\"%s\" sub-object=\"%s\" />\n";

        StringBuilder builder = new StringBuilder(prefix);

        public void append(LegacyType2Family mapping) {
            builder.append(String.format(itemFormat, mapping.getId(), mapping.getObjFamily(),
                    mapping.getDisplayName(), mapping.isSubObject()));
        }

        protected String build() {
            return builder.append(postfix).toString();
        }
    }
}
