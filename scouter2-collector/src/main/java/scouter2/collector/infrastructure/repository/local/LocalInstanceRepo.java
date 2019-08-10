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

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.Atomic;
import org.mapdb.HTreeMap;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import scouter2.collector.domain.instance.Instance;
import scouter2.collector.domain.instance.InstanceRepo;
import scouter2.collector.infrastructure.mapdb.CommonDb;
import scouter2.collector.springconfig.RepoTypeMatch;
import scouter2.collector.springconfig.RepoTypeSelectorCondition;
import scouter2.proto.InstanceP;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-17
 */
@Slf4j
@Component
@Conditional(RepoTypeSelectorCondition.class)
@RepoTypeMatch("local")
public class LocalInstanceRepo extends LocalRepoAdapter implements InstanceRepo {

    private CommonDb mapDb;
    HTreeMap<Long, byte[]> instanceMap;
    HTreeMap<String, Long> instanceNameIdMap;
    Atomic.Long instanceIdGenerator;

    public LocalInstanceRepo(CommonDb mapDb) {
        this.mapDb = mapDb;
        instanceMap = mapDb.getInstanceMap();
        instanceNameIdMap = mapDb.getInstanceNameIdMap();
        instanceIdGenerator = mapDb.getInstanceIdGenerator();
    }

    @Override
    public void add(Instance instance) {
        instanceMap.put(instance.getInstanceId(), instance.getProto().toByteArray());
        instanceNameIdMap.put(instance.getProto().getInstanceFullName(), instance.getInstanceId());
    }

    @Override
    public long findIdByName(String instanceFullName) {
        Long id = instanceNameIdMap.get(instanceFullName);
        return id == null ? 0 : id;
    }

    @Override
    public long generateUniqueIdByName(String instanceFullName) {
        Long id = instanceNameIdMap.get(instanceFullName);
        if (id == null) {
            long newId = instanceIdGenerator.incrementAndGet();
            //There can exist something of legacy objHash.
            if (instanceMap.containsKey(newId)) {
                return generateUniqueIdByName(instanceFullName);
            }
            return newId;
        } else {
            return id;
        }
    }

    @Override
    public Instance findById(long instanceId) {
        byte[] instanceProto = instanceMap.get(instanceId);
        if (instanceProto == null) {
            return null;
        }
        try {
            return new Instance(instanceId, InstanceP.parseFrom(instanceProto));

        } catch (InvalidProtocolBufferException e) {
            log.error("Error on parse instance proto from local db. id:{}", instanceId, e);
            return null;
        }
    }
}