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

import com.linecorp.armeria.common.HttpHeaderNames;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.grpc.GrpcSerializationFormats;
import com.linecorp.armeria.server.ClientAddressSource;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcServiceBuilder;
import com.linecorp.armeria.server.logging.LoggingService;
import io.grpc.ServerInterceptors;
import lombok.extern.slf4j.Slf4j;
import scouter2.collector.common.ShutdownManager;

import java.util.concurrent.CompletableFuture;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-10
 */
@Slf4j
public class Scouter2Transport {

    private final int port;
    private final Server server;

    public Scouter2Transport(int port) {
        this.port = port;
        ServerBuilder sb = new ServerBuilder();
        sb.http(port);
        sb.clientAddressSources(ClientAddressSource.ofHeader(HttpHeaderNames.FORWARDED),
                ClientAddressSource.ofHeader(HttpHeaderNames.X_FORWARDED_FOR),
                ClientAddressSource.ofProxyProtocol());

        sb.requestTimeoutMillis(5000);

        sb.service("/", (ctx, res) -> HttpResponse.of("Hello, world!"));

        sb.service(new GrpcServiceBuilder()
                        .addService(ServerInterceptors.intercept(
                                new XlogGrpcService(),
                                new GrpcAuthInterceptor()
                        ))
                        .enableUnframedRequests(true)
                        .supportedSerializationFormats(GrpcSerializationFormats.values())
                        .build(),
                LoggingService.newDecorator()
        );
        server = sb.build();
    }

    /**
     * Start serving requests.
     */
    public void start() {
        CompletableFuture<Void> future = server.start();
        log.info("Scouter collector GRPC server listening on " + port);

        ShutdownManager.getInstance().register1st(() -> {
            log.info("shutting down gRPC server since JVM is shutting down");
            Scouter2Transport.this.stop();
            log.info("GRPC server shut down");
        });

        future.join();
    }

    /**
     * Stop serving requests and shutdown resources.
     */
    public void stop() {
        server.close();
    }
}
