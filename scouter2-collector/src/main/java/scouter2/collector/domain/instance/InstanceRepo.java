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

import scouter2.collector.domain.ScouterRepo;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-08
 */
public interface InstanceRepo extends ScouterRepo {
    /**
     * persist instance
     * @param instance
     */
    void add(Instance instance);

    /**
     * find instance id by instance full name
     * @param instanceFullName
     * @return
     */
    long findIdByName(String instanceFullName);

    /**
     * generate (unique) instance id of the instance full name if not exists.
     *  - less value is better for serialization.
     * if exists return it.
     * @param instanceFullName
     * @return
     */
    long generateUniqueIdByName(String instanceFullName);

    /**
     * find instance by id
     * @param instanceId
     * @return
     */
    Instance findById(long instanceId);
}
