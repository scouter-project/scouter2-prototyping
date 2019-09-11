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

import com.google.protobuf.ByteString;
import org.eclipse.collections.api.list.MutableList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import scouter2.collector.LocalRepoTest;
import scouter2.collector.domain.profile.Profile;
import scouter2.common.util.ThreadUtil;
import scouter2.fixture.ProfileFixture;
import scouter2.proto.ProfileP;
import scouter2.proto.StepTypeP;
import scouter2.testsupport.T;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 07/09/2019
 */
public class LocalProfileRepoTest extends LocalRepoTest {

    String applicationId = "testapp";

    @Autowired
    LocalProfileRepo repo;

    @Test
    public void add_and_get_profiles() {
        Profile profile = ProfileFixture.getOne();

        repo.add(profile);

        MutableList<ProfileP> found = repo.findProfiles(profile.getProto().getTxid().toByteArray(),
                Integer.MAX_VALUE).getProfiles();
        assertThat(found.size()).isEqualTo(1);
        assertThat(found.get(0).getSteps()).isEqualTo(profile.getProto().getSteps());
        assertThat(found.get(0).getStepTypes(0)).isEqualTo(profile.getProto().getStepTypes(0));
    }

    @Test
    public void add_and_get_multi_profiles() {
        ByteString sameTxid = T.xlogIdAsBs();

        Profile profile0 = ProfileFixture.getOne();
        Profile profile11 = ProfileFixture.getOne(sameTxid, StepTypeP.DUMP);
        Profile profile12 = ProfileFixture.getOne(sameTxid, StepTypeP.APICALL);

        repo.add(profile0);
        repo.add(profile11);
        repo.add(profile12);

        MutableList<ProfileP> found = repo.findProfiles(sameTxid.toByteArray(), Integer.MAX_VALUE).getProfiles();
        assertThat(found.size()).isEqualTo(2);
        assertThat(found.get(0).getSteps()).isEqualTo(profile11.getProto().getSteps());
        assertThat(found.get(0).getStepTypes(0)).isEqualTo(profile11.getProto().getStepTypes(0));
        assertThat(found.get(1).getStepTypes(0)).isEqualTo(profile12.getProto().getStepTypes(0));
    }

    @Test
    public void add_and_get_multi_profiles_after_offset_index_buffer_flush() {
        ByteString sameTxid = T.xlogIdAsBs();

        Profile profile0 = ProfileFixture.getOne();
        Profile profile11 = ProfileFixture.getOne(sameTxid, StepTypeP.DUMP);
        Profile profile12 = ProfileFixture.getOne(sameTxid, StepTypeP.APICALL);

        repo.add(profile0);
        repo.add(profile11);
        repo.add(profile12);
        ThreadUtil.sleep(550);

        MutableList<ProfileP> found = repo.findProfiles(sameTxid.toByteArray(), Integer.MAX_VALUE).getProfiles();
        assertThat(found.size()).isEqualTo(2);
        assertThat(found.get(0).getSteps()).isEqualTo(profile11.getProto().getSteps());
        assertThat(found.get(0).getStepTypes(0)).isEqualTo(profile11.getProto().getStepTypes(0));
        assertThat(found.get(1).getStepTypes(0)).isEqualTo(profile12.getProto().getStepTypes(0));
    }

}