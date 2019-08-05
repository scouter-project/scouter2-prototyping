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

package scouter2.collector.domain.instance;

import org.springframework.stereotype.Service;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-04
 */
@Service
public class InstanceService {
    InstanceRepo repo;
    InstanceServiceCache cache;

    public InstanceService(InstanceRepo repo, InstanceServiceCache cache) {
        this.repo = repo;
        this.cache = cache;
    }

    public long findIdByName(String instanceFullName) {
        return cache.findIdByName(instanceFullName);
    }

    public Instance findById(long instanceId) {
        return cache.findById(instanceId);
    }

    public long generateUniqueIdByName(String instanceFullName) {
        return cache.generateUniqueIdByName(instanceFullName);
    }
}
