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

package scouter2.collector.infrastructure.repository.local;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import scouter2.collector.LocalRepoTest;
import scouter2.collector.domain.obj.Obj;
import scouter2.fixture.ObjFixture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-07
 */
public class LocalObjRepoTest extends LocalRepoTest {

    @Autowired
    LocalObjRepo sut;

    @Test
    public void generateUniqueIdByName() {
        long id1 = sut.generateUniqueIdByName("obj1");
        long id2 = sut.generateUniqueIdByName("obj2");

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    public void add_and_find_by_id() {
        long objId = sut.generateUniqueIdByName("obj1");
        Obj fixture = ObjFixture.getOne(objId);

        sut.add(fixture);
        Obj actual = sut.findById(fixture.getObjId());

        sut.findById(fixture.getObjId());
        sut.findById(fixture.getObjId());

        assertThat(actual).isEqualTo(fixture);
    }

    @Test
    public void add_and_find_all() {

        Obj fixture1 = ObjFixture.getOne(sut.generateUniqueIdByName("obj1"));
        Obj fixture2 = ObjFixture.getOne(sut.generateUniqueIdByName("obj2"));
        Obj fixture3 = ObjFixture.getOne(sut.generateUniqueIdByName("obj3"));

        sut.add(fixture1);
        sut.add(fixture2);
        sut.add(fixture3);

        MutableList<Obj> actuals = Lists.adapt(sut.findAll());

        assertThat(actuals.size()).isGreaterThanOrEqualTo(3);
        assertThat(actuals.select(e -> e.getObjId() == fixture1.getObjId()).getFirst()).isEqualTo(fixture1);
        assertThat(actuals.select(e -> e.getObjId() == fixture2.getObjId()).getFirst()).isEqualTo(fixture2);
        assertThat(actuals.select(e -> e.getObjId() == fixture3.getObjId()).getFirst()).isEqualTo(fixture3);
    }
}