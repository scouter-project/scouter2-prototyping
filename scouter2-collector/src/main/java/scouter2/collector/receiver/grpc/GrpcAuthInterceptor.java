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

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-13
 */
@Slf4j
public class GrpcAuthInterceptor implements ServerInterceptor {
    public static final Context.Key<Object> USER_IDENTITY
            = Context.key("identity"); // "identity" is just for debugging

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        log.info("[GrpcAuthInterceptor] interceptCall()");

        // You need to implement validateIdentity
        String identity = validateIdentity(headers);
        if (identity == null) { // this is optional, depending on your needs
            // Assume user not authenticated
            call.close(Status.UNAUTHENTICATED.withDescription("some more info"),
                    new Metadata());
            return new ServerCall.Listener() {};
        }
        Context context = Context.current().withValue(USER_IDENTITY, identity);
        return Contexts.interceptCall(context, call, headers, next);
    }

    private String validateIdentity(Metadata headers) {
        return "gunlee";
    }
}
