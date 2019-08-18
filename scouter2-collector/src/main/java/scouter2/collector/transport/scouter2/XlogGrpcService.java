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

package scouter2.collector.transport.scouter2;

import com.google.protobuf.TextFormat;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.proto.CountingP;
import scouter2.proto.XlogListP;
import scouter2.proto.XlogP;
import scouter2.proto.XlogSearchP;
import scouter2.proto.XlogServiceGrpc;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-10
 */
@Slf4j
public class XlogGrpcService extends XlogServiceGrpc.XlogServiceImplBase {

    public static final ThrottleConfig S_0024 = ThrottleConfig.of("S0024");

    @Override
    public void getXlogByTxid(XlogSearchP request, StreamObserver<XlogP> responseObserver) {
        log.info("[getXlogByTxid] start, xlogSearch:" + TextFormat.shortDebugString(request));
        responseObserver.onNext(XlogP.newBuilder().setTxid(100).setPtxid(1000).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getXlogByGxid(XlogSearchP request, StreamObserver<XlogListP> responseObserver) {
        super.getXlogByGxid(request, responseObserver);
    }

    @Override
    public StreamObserver<XlogP> addXlog(StreamObserver<CountingP> responseObserver) {
        AtomicLong atomicLong = new AtomicLong();

        return new StreamObserver<XlogP>() {
            @Override
            public void onNext(XlogP xlog) {
                log.info("id=" + GrpcAuthInterceptor.USER_IDENTITY.get());
                atomicLong.incrementAndGet();
                log.debug("received xlog:" + TextFormat.shortDebugString(xlog));
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error on GrpcXlogCollector.onError()", S_0024, t);
            }

            @Override
            public void onCompleted() {
                CountingP counting = CountingP.newBuilder()
                        .setValue(atomicLong.get())
                        .build();
                responseObserver.onNext(counting);
                responseObserver.onCompleted();
            }
        };
    }
}
