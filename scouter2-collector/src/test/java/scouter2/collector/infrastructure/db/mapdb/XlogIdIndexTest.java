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

package scouter2.collector.infrastructure.db.mapdb;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import scouter2.collector.LocalRepoTest;
import scouter2.collector.config.ConfigCommon;
import scouter2.testsupport.T;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 05/09/2019
 */
public class XlogIdIndexTest extends LocalRepoTest {

    @Autowired
    ConfigCommon configCommon;

    @Autowired
    ConfigMapDb configMapDb;

    @Test
    public void test() {
        XlogIdIndex index = new XlogIdIndex("20190101", configCommon, configMapDb);

        byte[] gxid = T.xlogId();
        byte[] txid1 = T.xlogId();
        byte[] txid2 = T.xlogId();
        index.getGxidIndex().put(gxid, Lists.mutable.of(txid1, txid2));

        MutableList<byte[]> idList = index.getGxidIndex().get(gxid);

        assertArrayEquals(txid1, idList.get(0));
        assertArrayEquals(txid2, idList.get(1));
    }
}