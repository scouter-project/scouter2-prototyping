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

package scouter2.collector.infrastructure.db.filedb;

import org.apache.commons.io.FileUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.primitive.LongSets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scouter2.collector.common.util.U;
import scouter2.collector.config.ConfigCommon;
import scouter2.collector.domain.xlog.Xlog;
import scouter2.fixture.XlogFixture;
import scouter2.proto.XlogP;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 27/08/2019
 */
public class XlogFileDbTest {

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
        XlogFileDb db = new XlogFileDb(configCommon);

        Xlog xlog = XlogFixture.getOne();
        long add = db.add(pKey, xlog);
    }

    @Test
    public void add_and_read() throws IOException {
        XlogFileDb db = new XlogFileDb(configCommon);
        long now = System.currentTimeMillis();

        int objId = 200;
        Xlog xlog1 = XlogFixture.getOne(Instant.now().toEpochMilli(), objId);
        Xlog xlog2 = XlogFixture.getOne(Instant.now().toEpochMilli() + 100, objId);

        long startOffset = db.add(pKey, xlog1);
        db.add(pKey, xlog2);

        Map<Long, XlogP> map = Maps.mutable.of(
                xlog1.getProto().getTxid(), xlog1.getProto(),
                xlog2.getProto().getTxid(), xlog2.getProto()
        );
        MutableList<XlogP> readList = Lists.mutable.empty();

        //when
        db.readPeriod(pKey, startOffset, System.currentTimeMillis() + 1000,
                LongSets.mutable.of(xlog1.getProto().getObjId()),
                readList::add);

        //then
        assertThat(readList.size()).isEqualTo(2);
        readList.forEach(read -> assertThat(read.getTxid()).isEqualTo(map.get(read.getTxid()).getTxid()));
    }

    @Test
    public void add_and_read_with_obj_and_cutoff_at_endMillis_condition() throws IOException {
        XlogFileDb db = new XlogFileDb(configCommon);

        int objId1 = 100;
        int objId2 = 200;
        int objId3 = 200;
        long now = Instant.now().toEpochMilli();
        Xlog xlog1 = XlogFixture.getOne(now, objId1);
        Xlog xlog21 = XlogFixture.getOne(now + 100, objId2);
        Xlog xlog22 = XlogFixture.getOne(now + 200, objId2);
        Xlog xlog31 = XlogFixture.getOne(now + 300, objId3);
        Xlog xlog32 = XlogFixture.getOne(now + 3000, objId3);
        Xlog xlog12 = XlogFixture.getOne(now + 3100, objId1);
        Xlog xlog33 = XlogFixture.getOne(now + 3200, objId3);

        long startOffset = db.add(pKey, xlog1);
        db.add(pKey, xlog21);
        db.add(pKey, xlog22);
        db.add(pKey, xlog31);
        db.add(pKey, xlog32);
        db.add(pKey, xlog12);
        db.add(pKey, xlog33);

        Map<Long, XlogP> map = Maps.mutable.of(
                xlog1.getProto().getTxid(), xlog1.getProto(),
                xlog21.getProto().getTxid(), xlog21.getProto(),
                xlog22.getProto().getTxid(), xlog22.getProto(),
                xlog31.getProto().getTxid(), xlog31.getProto()
        );
        MutableList<XlogP> readList = Lists.mutable.empty();

        //when
        db.readPeriod(pKey, startOffset, now + 2000,
                LongSets.mutable.of(objId1, objId2),
                readList::add);

        //then
        assertThat(readList.size()).isEqualTo(4);
        readList.forEach(read -> assertThat(read.getTxid()).isEqualTo(map.get(read.getTxid()).getTxid()));
    }
}