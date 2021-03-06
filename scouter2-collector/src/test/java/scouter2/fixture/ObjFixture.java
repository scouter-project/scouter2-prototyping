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

package scouter2.fixture;

import scouter2.collector.domain.obj.Obj;
import scouter2.proto.ObjP;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-15
 */
public class ObjFixture {

    public static Obj getOne(long objId) {
        ObjP proto = ObjP.newBuilder()
                .setApplicationId("test-application")
                .setLegacyObjHash(100)
                .setAddress("10.0.0.1")
                .setObjFullName("/test/java01")
                .setObjLegacyType("tomcat")
                .setObjFamily("javaee")
                .setVersion("0.0.1")
                .build();

        return new Obj(objId, proto);
    }

    public static Obj getOne(long objId, String applicationId) {
        ObjP proto = ObjP.newBuilder()
                .setApplicationId(applicationId)
                .setLegacyObjHash(100)
                .setAddress("10.0.0.1")
                .setObjFullName("/test/java01")
                .setObjLegacyType("tomcat")
                .setObjFamily("javaee")
                .setVersion("0.0.1")
                .build();

        return new Obj(objId, proto);
    }

    public static Obj getOneOfType(long objId, String objType) {
        ObjP proto = ObjP.newBuilder()
                .setApplicationId("test-application")
                .setLegacyObjHash(100)
                .setAddress("10.0.0.1")
                .setObjFullName("/test/java01")
                .setObjLegacyType(objType)
                .setObjFamily("javaee")
                .setVersion("0.0.1")
                .build();

        return new Obj(objId, proto);
    }

    public static Obj getOneOfFamily(long objId, String family) {
        ObjP proto = ObjP.newBuilder()
                .setApplicationId("test-application")
                .setLegacyObjHash(100)
                .setAddress("10.0.0.1")
                .setObjFullName("/test/java01")
                .setObjLegacyType("tomcat")
                .setObjFamily(family)
                .setVersion("0.0.1")
                .build();

        return new Obj(objId, proto);
    }
}
