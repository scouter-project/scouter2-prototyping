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

package scouter2.common.meta;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-18
 */
public class LegacyType2Family {
    String id;
    String objFamily;
    String displayName;
    boolean subObject;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObjFamily() {
        return objFamily;
    }

    public void setObjFamily(String objFamily) {
        this.objFamily = objFamily;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isSubObject() {
        return subObject;
    }

    public void setSubObject(boolean subObject) {
        this.subObject = subObject;
    }

    @Override
    public String toString() {
        return "LegacyType2Family{" +
                "id='" + id + '\'' +
                ", objFamily='" + objFamily + '\'' +
                ", displayName='" + displayName + '\'' +
                ", subObject=" + subObject +
                '}';
    }
}
