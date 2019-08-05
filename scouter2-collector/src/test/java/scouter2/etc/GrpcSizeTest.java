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

package scouter2.etc;

import org.junit.Test;
import scouter2.proto.TestObj;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-08-04
 */
public class GrpcSizeTest {

    @Test
    public void grpcIntVariableEncodingTest() {
        TestObj forSmallInt = TestObj.newBuilder().setInstanceHash(1).build();
        TestObj forMidlInt = TestObj.newBuilder().setInstanceHash(256*2).build();
        TestObj forBiglInt = TestObj.newBuilder().setInstanceHash(65536*2).build();
        TestObj forVeryBiglInt = TestObj.newBuilder().setInstanceHash(Integer.MAX_VALUE).build();

        assertThat(forSmallInt.toByteArray().length).isEqualTo(2);
        assertThat(forMidlInt.toByteArray().length).isEqualTo(3);
        assertThat(forBiglInt.toByteArray().length).isEqualTo(4);
        assertThat(forVeryBiglInt.toByteArray().length).isEqualTo(6);
    }

    @Test
    public void grpcLongVariableEncodingTest() {
        TestObj forSmall = TestObj.newBuilder().setInstanceHash64(1).build();
        TestObj forMid = TestObj.newBuilder().setInstanceHash64(65536*2).build();
        TestObj forBig = TestObj.newBuilder().setInstanceHash64(Integer.MAX_VALUE/2).build();
        TestObj forVeryBig = TestObj.newBuilder().setInstanceHash64(((long)Integer.MAX_VALUE)*1024).build();
        TestObj forVeryVeryBig = TestObj.newBuilder().setInstanceHash64(Long.MAX_VALUE/2).build();

        assertThat(forSmall.toByteArray().length).isEqualTo(2);
        assertThat(forMid.toByteArray().length).isEqualTo(4);
        assertThat(forBig.toByteArray().length).isEqualTo(6);
        assertThat(forVeryBig.toByteArray().length).isEqualTo(7);
        assertThat(forVeryVeryBig.toByteArray().length).isEqualTo(10);
    }

    @Test
    public void grpcMinusLongVariableEncodingTest() {
        TestObj forSmall = TestObj.newBuilder().setInstanceHash64(-1).build();
        TestObj forMid = TestObj.newBuilder().setInstanceHash64(-65536*2).build();
        TestObj forBig = TestObj.newBuilder().setInstanceHash64(Integer.MAX_VALUE/-2).build();
        TestObj forVeryBig = TestObj.newBuilder().setInstanceHash64(((long)Integer.MAX_VALUE)*-1024).build();
        TestObj forVeryVeryBig = TestObj.newBuilder().setInstanceHash64(Long.MAX_VALUE/-2).build();

        assertThat(forSmall.toByteArray().length).isEqualTo(11);
        assertThat(forMid.toByteArray().length).isEqualTo(11);
        assertThat(forBig.toByteArray().length).isEqualTo(11);
        assertThat(forVeryBig.toByteArray().length).isEqualTo(11);
        assertThat(forVeryVeryBig.toByteArray().length).isEqualTo(11);
    }

    @Test
    public void grpcSLongVariableEncodingTest() {
        TestObj forSmall = TestObj.newBuilder().setInstanceHashS64(1).build();
        TestObj forMid = TestObj.newBuilder().setInstanceHashS64(65536*2).build();
        TestObj forBig = TestObj.newBuilder().setInstanceHashS64(Integer.MAX_VALUE/2).build();
        TestObj forVeryBig = TestObj.newBuilder().setInstanceHashS64(((long)Integer.MAX_VALUE)*1024).build();
        TestObj forVeryVeryBig = TestObj.newBuilder().setInstanceHashS64(Long.MAX_VALUE/2).build();

        assertThat(forSmall.toByteArray().length).isEqualTo(2);
        assertThat(forMid.toByteArray().length).isEqualTo(4);
        assertThat(forBig.toByteArray().length).isEqualTo(6);
        assertThat(forVeryBig.toByteArray().length).isEqualTo(7);
        assertThat(forVeryVeryBig.toByteArray().length).isEqualTo(10);
    }

    @Test
    public void grpcMinusSLongVariableEncodingTest() {
        TestObj forSmall = TestObj.newBuilder().setInstanceHashS64(-1).build();
        TestObj forMid = TestObj.newBuilder().setInstanceHashS64(65536*-2).build();
        TestObj forBig = TestObj.newBuilder().setInstanceHashS64(Integer.MAX_VALUE/-2).build();
        TestObj forVeryBig = TestObj.newBuilder().setInstanceHashS64(((long)Integer.MAX_VALUE)*-1024).build();
        TestObj forVeryVeryBig = TestObj.newBuilder().setInstanceHashS64(Long.MAX_VALUE/-2).build();

        assertThat(forSmall.toByteArray().length).isEqualTo(2);
        assertThat(forMid.toByteArray().length).isEqualTo(4);
        assertThat(forBig.toByteArray().length).isEqualTo(6);
        assertThat(forVeryBig.toByteArray().length).isEqualTo(7);
        assertThat(forVeryVeryBig.toByteArray().length).isEqualTo(10);
    }

    @Test
    public void grpcLongKeyVariableEncodingTest() {
        TestObj forSmall = TestObj.newBuilder().putTags64(1, "").build();
        TestObj forMid = TestObj.newBuilder().putTags64(65536*2, "").build();
        TestObj forBig = TestObj.newBuilder().putTags64(Integer.MAX_VALUE/2, "").build();
        TestObj forVeryBig = TestObj.newBuilder().putTags64(((long)Integer.MAX_VALUE)*1024, "").build();
        TestObj forVeryVeryBig = TestObj.newBuilder().putTags64(Long.MAX_VALUE/2, "").build();

        assertThat(forSmall.toByteArray().length).isEqualTo(6);
        assertThat(forMid.toByteArray().length).isEqualTo(8);
        assertThat(forBig.toByteArray().length).isEqualTo(10);
        assertThat(forVeryBig.toByteArray().length).isEqualTo(11);
        assertThat(forVeryVeryBig.toByteArray().length).isEqualTo(14);
    }
}
