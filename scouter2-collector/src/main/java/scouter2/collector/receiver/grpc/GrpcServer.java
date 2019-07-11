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

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import scouter2.collector.common.ShutdownManager;

import java.io.IOException;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-10
 */
@Slf4j
public class GrpcServer {

    private final int port;
    private final Server server;

    public GrpcServer(int port) {
        this.port = port;
        server = ServerBuilder.forPort(port)
                .addService(new GrpcXlogCollector())
                .build();
    }

    /**
     * Start serving requests.
     */
    public void start() throws IOException {
        server.start();
        log.info("Scouter collector GRPC server listening on " + port);
        ShutdownManager.getInstance().register(() -> {
            log.info("shutting down gRPC server since JVM is shutting down");
            GrpcServer.this.stop();
            log.info("GRPC server shut down");
        });
    }

    /**
     * Stop serving requests and shutdown resources.
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
