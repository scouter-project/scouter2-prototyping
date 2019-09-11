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

import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.LongList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.LongLists;
import org.mapdb.HTreeMap;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.domain.NonThreadSafeRepo;
import scouter2.collector.domain.profile.Profile;
import scouter2.collector.domain.profile.ProfileFragments;
import scouter2.collector.domain.profile.ProfileRepo;
import scouter2.collector.domain.profile.ProfileRepoAdapter;
import scouter2.collector.infrastructure.db.filedb.ProfileFileDb;
import scouter2.collector.infrastructure.db.mapdb.ProfileDb;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.common.support.XlogIdSupport;
import scouter2.common.util.DateUtil;
import scouter2.proto.ProfileP;

import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-17
 */
@Slf4j
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("local")
public class LocalProfileRepo extends ProfileRepoAdapter implements ProfileRepo, NonThreadSafeRepo {

    public static final ThrottleConfig S_0061 = ThrottleConfig.of("S0061");
    public static final ThrottleConfig S_0062 = ThrottleConfig.of("S0062");

    @Override
    public String getRepoType() {
        return LocalRepoConstant.TYPE_NAME;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    private ProfileFileDb profileFileDb;
    private ProfileDb profileDb;
    LocalProfileOffsetBuffer offsetBuffer;

    public LocalProfileRepo(ProfileFileDb profileFileDb, ProfileDb profileDb,
                            LocalProfileOffsetBuffer offsetBuffer) {
        this.profileFileDb = profileFileDb;
        this.profileDb = profileDb;
        this.offsetBuffer = offsetBuffer;
    }

    @Override
    public void add(Profile profile) {
        try {
            if (profile == null || profile.getProto() == null) {
                return;
            }
            add2Db(profile);

        } catch (Exception e) {
            log.error(e.getMessage(), S_0061, e);
        }
    }

    private void add2Db(Profile profile) throws IOException {
        String ymd = DateUtil.yyyymmdd(profile.getTimestamp());
        long offset = profileFileDb.add(ymd, profile.getProto());
        if (offset >= 0) {
            indexing(ymd, profile.getProto(), offset);
        }
    }

    private void indexing(String ymd, ProfileP profileP, long offset) {
        offsetBuffer.add(profileP.getTxid().toByteArray(), offset, profileP.getTimestamp(), profileDb.getIndex(ymd));
    }

    @Override
    public ProfileFragments findProfiles(byte[] txid, int maxProfileFragmentCount) {
        if (txid == null) {
            return null;
        }
        MutableList<ProfileP> profiles = Lists.mutable.empty();
        int profileCount;

        String ymd = DateUtil.yyyymmdd(XlogIdSupport.timestamp(txid));
        HTreeMap<byte[], LongList> offsetIndex = profileDb.getIndex(ymd);
        try {
            LongList offsets = getOffsets(txid, offsetIndex);
            profileCount = offsets.size();
            int retrieveMax = Math.min(profileCount, maxProfileFragmentCount);

            for (int i = 0; i < retrieveMax; i++) {
                ProfileP profileP = profileFileDb.get(ymd, offsets.get(i));
                if (profileP != null) {
                    profiles.add(profileP);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), S_0062, e);
            throw new RuntimeException(e.getMessage(), e);
        }

        return new ProfileFragments(profiles, profileCount);
    }

    private LongList getOffsets(byte[] txid, HTreeMap<byte[], LongList> offsetIndex) {
        LongList offsets = offsetBuffer.getOffsetList(txid);
        if (offsets == null) {
            offsets = offsetIndex.get(txid);
        }
        if (offsets == null) {
            offsets = LongLists.mutable.empty();
        }
        return offsets;
    }
}
