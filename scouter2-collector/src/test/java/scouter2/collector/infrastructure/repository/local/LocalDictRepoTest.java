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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import scouter2.collector.LocalRepoTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 31/08/2019
 */
public class LocalDictRepoTest extends LocalRepoTest {

    @Autowired
    LocalDictRepo sut;

    @Test
    public void add_and_get() {
        String service100 = "service100";
        String service200 = "service200";
        String error100 = "error100";

        sut.addOrModify("anyApp", "service", 100, service100);
        sut.addOrModify("anyApp", "service", 200, service200);
        sut.addOrModify("anyApp", "error", 100, error100);


        assertThat(sut.find("", "service", 100)).isEqualTo(service100);
        assertThat(sut.find("", "service", 200)).isEqualTo(service200);
        assertThat(sut.find("", "error", 100)).isEqualTo(error100);
        assertThat(sut.find("", "error", 200)).isNull();
        assertThat(sut.find("", "any-no-exist", 100)).isNull();

    }
}