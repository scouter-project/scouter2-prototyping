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

import static org.assertj.core.api.Assertions.assertThat;

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

        sut.addOrModify(fixture);
        Obj actual = sut.findById(fixture.getObjId());

        sut.findById(fixture.getObjId());
        sut.findById(fixture.getObjId());

        assertThat(actual).isEqualTo(fixture);
    }

    @Test
    public void find_by_application_id() {
        String appl1 = "app1";
        String appl2 = "appl2";

        Obj fixture1 = ObjFixture.getOne(sut.generateUniqueIdByName("obj1"), appl1);
        Obj fixture2 = ObjFixture.getOne(sut.generateUniqueIdByName("obj2"), appl1);
        Obj anotherFixture = ObjFixture.getOne(sut.generateUniqueIdByName("objAnother"), appl2);

        sut.addOrModify(fixture1);
        sut.addOrModify(fixture2);
        sut.addOrModify(anotherFixture);

        MutableList<Obj> actuals = Lists.adapt(sut.findByApplicationId(appl1));

        assertThat(actuals.size()).isEqualTo(2);
        assertThat(actuals).containsOnly(fixture1, fixture2);
    }

    @Test
    public void find_by_legacy_obj_type() {
        Obj fixture1 = ObjFixture.getOneOfType(sut.generateUniqueIdByName("obj1"), "tomcat");
        Obj fixture2 = ObjFixture.getOneOfType(sut.generateUniqueIdByName("obj2"), "tomcat");
        Obj anotherFixture = ObjFixture.getOneOfType(sut.generateUniqueIdByName("objAnother"), "another");

        sut.addOrModify(fixture1);
        sut.addOrModify(fixture2);
        sut.addOrModify(anotherFixture);

        MutableList<Obj> actuals = Lists.adapt(sut.findByLegacyObjType(fixture1.getApplicationId(), "tomcat"));

        assertThat(actuals.size()).isEqualTo(2);
        assertThat(actuals).containsOnly(fixture1, fixture2);
    }

    @Test
    public void find_by_family() {
        Obj fixture1 = ObjFixture.getOneOfFamily(sut.generateUniqueIdByName("obj1"), "javaee");
        Obj fixture2 = ObjFixture.getOneOfFamily(sut.generateUniqueIdByName("obj2"), "javaee");
        Obj anotherFixture = ObjFixture.getOneOfFamily(sut.generateUniqueIdByName("objAnother"), "another");

        sut.addOrModify(fixture1);
        sut.addOrModify(fixture2);
        sut.addOrModify(anotherFixture);

        MutableList<Obj> actuals = Lists.adapt(sut.findByFamily(fixture1.getApplicationId(), "javaee"));

        assertThat(actuals.size()).isEqualTo(2);
        assertThat(actuals).containsOnly(fixture1, fixture2);
    }
}