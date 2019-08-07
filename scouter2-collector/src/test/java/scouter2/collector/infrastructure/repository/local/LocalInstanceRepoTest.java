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
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-07
 */
public class LocalInstanceRepoTest extends LocalRepoTest {

    @Autowired
    LocalInstanceRepo repo;

    @Test
    public void generateUniqueIdByName() {
        long id1 = repo.generateUniqueIdByName("instance1");
        long id2 = repo.generateUniqueIdByName("instance2");

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }
}