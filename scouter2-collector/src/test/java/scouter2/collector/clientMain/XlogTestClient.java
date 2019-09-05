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

package scouter2.collector.clientMain;

import com.google.common.base.Stopwatch;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import scouter2.proto.CountingP;
import scouter2.proto.XlogP;
import scouter2.proto.XlogServiceGrpc;
import scouter2.testsupport.T;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-10
 */
@Slf4j
public class XlogTestClient {

    private final ManagedChannel channel;
    private final XlogServiceGrpc.XlogServiceBlockingStub blockingStub;
    private final XlogServiceGrpc.XlogServiceStub asyncStub;

    public XlogTestClient(String host, int port){
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    public XlogTestClient(ManagedChannelBuilder < ? > channelBuilder){
        channel = channelBuilder.build();
        blockingStub = XlogServiceGrpc.newBlockingStub(channel);
        asyncStub = XlogServiceGrpc.newStub(channel);
    }

    public void shutdown () throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {

        XlogTestClient client = new XlogTestClient("localhost", 6200);
        client.xlogAdd();
    }

    private void xlogAdd() throws InterruptedException {
        log.info("addXlogsTest");
        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<CountingP> responseObserver = new StreamObserver<CountingP>() {
            @Override
            public void onNext(CountingP counting) {
                System.out.println("onNext counting : " + counting.getValue());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("onError  : " + t.getMessage());
                log.error("error", t);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("onCompleted");
                finishLatch.countDown();
            }
        };

        StreamObserver<XlogP> requestObserver = asyncStub.addXlog(responseObserver);
        AtomicLong atomicLong = new AtomicLong();
        Stopwatch stopwatch = Stopwatch.createStarted();

        while (true) {
            try {
                try {
                    for (int i = 1; i < 100; ++i) {
                        XlogP xlog = XlogP.newBuilder()
                                .setTxid(T.xlogIdAsBs())
                                .build();

                        requestObserver.onNext(xlog);
                        if (atomicLong.get() % 10 == 0) {
                            long elapsed = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                            System.out.println("[LOOP]=" + atomicLong.get() + ", elapsed=" + elapsed);
                        }
                    }
                } catch (RuntimeException e) {
                    requestObserver.onError(e);
                    throw e;
                }
                requestObserver.onCompleted();

            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        }

        //Receiving happens asynchronously
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.out.println("recordRoute can not finish within 1 minutes");
        }
    }
}
