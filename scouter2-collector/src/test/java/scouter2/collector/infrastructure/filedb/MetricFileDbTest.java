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

package scouter2.collector.infrastructure.filedb;

import org.apache.commons.io.FileUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigCommon;
import scouter2.proto.Metric4RepoP;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static scouter2.fixture.Metric4RepoPFixture.getMetric4RepoP;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-06
 */
public class MetricFileDbTest {

    public static final String DB_DIR = U.systemTmpDir() + "scouter2/database";
    ConfigCommon configCommon = ConfigCommon.builder().dbDir(DB_DIR).build();

    long instanceId = 1;
    String pKey = "20190101";



    @BeforeClass
    public static void beforeClass() {
        System.setProperty("repoType", "local");
        File file = new File(DB_DIR);
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void afterClass() {
        File file = new File(DB_DIR);
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void add() throws IOException {
        MetricFileDb db = new MetricFileDb(configCommon);

        Metric4RepoP metric = getMetric4RepoP(instanceId, 10000, 10.0d, 100.0d);
        long add = db.add(pKey, metric);
    }

    @Test
    public void add_and_read() throws IOException {
        MetricFileDb db = new MetricFileDb(configCommon);
        long now = System.currentTimeMillis();

        Metric4RepoP metric = getMetric4RepoP(instanceId, now - 10000, 10, 100);
        Metric4RepoP metric2 = getMetric4RepoP(instanceId, now - 8000, 20, 200);

        Map<Long, Metric4RepoP> metricMap = Maps.mutable.of(
                metric.getTimestamp(), metric,
                metric2.getTimestamp(), metric2
        );

        long offset = db.add(pKey, metric);
        db.add(pKey, metric2);

        MutableList<Metric4RepoP> readList = Lists.mutable.empty();

        //when
        db.readPeriod(pKey, offset, System.currentTimeMillis(), readList::add);

        //then
        assertThat(readList.size()).isEqualTo(2);
        readList.forEach(read -> assertThat(read).isEqualTo(metricMap.get(read.getTimestamp())));
    }

    @Test
    public void add_and_read_cutoff_at_endMillis_condition() throws IOException {
        MetricFileDb db = new MetricFileDb(configCommon);
        long now = System.currentTimeMillis();

        Metric4RepoP metric = getMetric4RepoP(instanceId, now - 10000, 10, 100);
        Metric4RepoP metric2 = getMetric4RepoP(instanceId, now - 8000, 20, 200);

        Map<Long, Metric4RepoP> metricMap = Maps.mutable.of(
                metric.getTimestamp(), metric,
                metric2.getTimestamp(), metric2
        );

        long offset = db.add(pKey, metric);
        db.add(pKey, metric2);

        MutableList<Metric4RepoP> readList = Lists.mutable.empty();

        //when
        db.readPeriod(pKey, offset, now - 9000, readList::add);

        //then
        assertThat(readList.size()).isEqualTo(1);
        readList.forEach(read -> assertThat(read).isEqualTo(metricMap.get(read.getTimestamp())));
    }

    @Test
    public void add_and_read_over_buffer_size_4k() throws IOException {
        MetricFileDb db = new MetricFileDb(configCommon);
        long now = System.currentTimeMillis();

        //39byte per 1 (make 5k)
        int metricCount = 5000 / 39;
        long firstOffset = 0;

        for (int i = 0; i < metricCount; i++) {
            long offset = db.add(pKey, getMetric4RepoP(instanceId, now - ((metricCount + i) * 2000), i, i));
            if (i == 0) {
                firstOffset = offset;
            }
        }

        MutableList<Metric4RepoP> readList = Lists.mutable.empty();

        //when
        db.readPeriod(pKey, firstOffset, now - 9000, readList::add);

        //then
        assertThat(readList.size()).isEqualTo(metricCount);
        //readList.forEach(read -> assertThat(read).isEqualTo(metricMap.get(read.getTimestamp())));
    }
}