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

package scouter2.collector.receiver.grpc;

import com.google.protobuf.TextFormat;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import scouter2.proto.Counting;
import scouter2.proto.Xlog;
import scouter2.proto.XlogCollectorGrpc;
import scouter2.proto.XlogList;
import scouter2.proto.XlogSearch;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-10
 */
@Slf4j
public class GrpcXlogCollector extends XlogCollectorGrpc.XlogCollectorImplBase {
    @Override
    public void getXlogByTxid(XlogSearch request, StreamObserver<Xlog> responseObserver) {
        super.getXlogByTxid(request, responseObserver);
    }

    @Override
    public void getXlogByGxid(XlogSearch request, StreamObserver<XlogList> responseObserver) {
        super.getXlogByGxid(request, responseObserver);
    }

    @Override
    public StreamObserver<Xlog> addXlog(StreamObserver<Counting> responseObserver) {
        AtomicLong atomicLong = new AtomicLong();

        return new StreamObserver<Xlog>() {
            @Override
            public void onNext(Xlog xlog) {
                atomicLong.incrementAndGet();
                log.debug("received xlog:" + TextFormat.shortDebugString(xlog));
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error on GrpcXlogCollector.onError()", t);
            }

            @Override
            public void onCompleted() {
                Counting counting = Counting.newBuilder()
                        .setValue(atomicLong.get())
                        .build();
                responseObserver.onNext(counting);
                responseObserver.onCompleted();
            }
        };
    }
}
