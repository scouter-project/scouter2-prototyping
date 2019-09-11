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

package scouter2.collector.domain.profile;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.MutableList;
import org.springframework.stereotype.Component;
import scouter.io.DataOutputX;
import scouter.lang.step.MessageStep;
import scouter2.proto.ProfileP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-09-08
 */
@Component
@Slf4j
public class ProfileService {
    private static ProfileService instance;

    private ProfileRepo repo;

    public ProfileService(ProfileRepo repo) {
        synchronized (ProfileService.class) {
            if (instance != null) {
                throw new IllegalStateException();
            }
            this.repo = repo;
            instance = this;
        }
    }

    public static ProfileService getInstance() {
        return instance;
    }

    public byte[] findStepsAsLegacyType(byte[] txid, int maxProfileFragmentCount)
            throws IOException {

        ProfileFragments pf = repo.findProfiles(txid, maxProfileFragmentCount);
        if (pf.getProfileCount() == 0) {
            return new byte[0];
        }

        boolean isLegacyType = pf.getProfiles().getAny().getIsLegacySteps();
        if (isLegacyType) {
            return getStepsBytes(pf);
        } else {
            return getStepsBytesNonLegacyToLegacy(pf);
        }
    }

    private byte[] getStepsBytes(ProfileFragments pf) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MutableList<ProfileP> profiles = pf.getProfiles();
        for (ProfileP p : profiles) {
            out.write(p.getSteps().toByteArray());
        }
        if (profiles.size() < pf.getProfileCount()) {
            MessageStep omitStep = new MessageStep();
            omitStep.message = " ** Profiles Omitted ** ";
            out.write(new DataOutputX().writeStep(omitStep).toByteArray());
        }
        return out.toByteArray();
    }

    private byte[] getStepsBytesNonLegacyToLegacy(ProfileFragments pf) throws IOException {
        //TODO implements
        return new byte[0];
    }
}
