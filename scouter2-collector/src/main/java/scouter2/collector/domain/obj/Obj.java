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

package scouter2.collector.domain.obj;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import scouter2.proto.ObjP;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-03
 */
@Getter
@Slf4j
public class Obj implements Serializable {
    @TaggedFieldSerializer.Tag(10)
    long objId;
    @TaggedFieldSerializer.Tag(13)
    String applicationId;
    @TaggedFieldSerializer.Tag(16)
    String objFamily;
    @TaggedFieldSerializer.Tag(19)
    String objLegacyType;
    @TaggedFieldSerializer.Tag(22)
    String objFullName;
    @TaggedFieldSerializer.Tag(23)
    long objLegacyHash;
    @TaggedFieldSerializer.Tag(25)
    String address;
    @TaggedFieldSerializer.Tag(28)
    String version;

    @TaggedFieldSerializer.Tag(30)
    long lastActive;
    @TaggedFieldSerializer.Tag(32)
    boolean deleted;

    @TaggedFieldSerializer.Tag(55)
    Map<String, String> tags;

    public Obj(long objId, ObjP proto) {
        this.objId = objId;
        this.applicationId = proto.getApplicationId();
        this.objFamily = proto.getObjFamily();
        this.objLegacyType = proto.getObjLegacyType();
        this.objFullName = proto.getObjFullName();
        this.objLegacyHash = proto.getLegacyObjHash();
        this.address = proto.getAddress();
        this.version = proto.getVersion();
        this.tags = new HashMap<>(proto.getTagsMap());
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public String getFullNameOrLegacyHash() {
        if (objLegacyHash != 0) {
            return String.valueOf(objLegacyHash);
        }
        return objFullName;
    }

    public boolean isDead(long deadIntervalMillis, long now) {
        return now - lastActive > deadIntervalMillis;
    }

    @Override
    public String toString() {
        return "Obj{" +
                "objId=" + objId +
                ", applicationId='" + applicationId + '\'' +
                ", objFamily='" + objFamily + '\'' +
                ", objLegacyType='" + objLegacyType + '\'' +
                ", objFullName='" + objFullName + '\'' +
                ", address='" + address + '\'' +
                ", version='" + version + '\'' +
                ", tags=" + tags +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Obj obj = (Obj) o;

        if (objId != obj.objId) return false;
        if (applicationId != null ? !applicationId.equals(obj.applicationId) : obj.applicationId != null) return false;
        if (objFamily != null ? !objFamily.equals(obj.objFamily) : obj.objFamily != null) return false;
        if (objLegacyType != null ? !objLegacyType.equals(obj.objLegacyType) : obj.objLegacyType != null) return false;
        if (objFullName != null ? !objFullName.equals(obj.objFullName) : obj.objFullName != null) return false;
        if (address != null ? !address.equals(obj.address) : obj.address != null) return false;
        if (version != null ? !version.equals(obj.version) : obj.version != null) return false;
        return tags != null ? tags.equals(obj.tags) : obj.tags == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (objId ^ (objId >>> 32));
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
        result = 31 * result + (objFamily != null ? objFamily.hashCode() : 0);
        result = 31 * result + (objLegacyType != null ? objLegacyType.hashCode() : 0);
        result = 31 * result + (objFullName != null ? objFullName.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }
}
