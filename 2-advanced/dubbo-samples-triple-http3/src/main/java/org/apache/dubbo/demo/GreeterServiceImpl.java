/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.demo;

import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.demo.pb.HelloReply;
import org.apache.dubbo.demo.pb.HelloRequest;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DubboService
public class GreeterServiceImpl implements GreeterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreeterServiceImpl.class);

    private static final StringBuffer finalString = new StringBuffer();

    static {
        IntStream.range(0, 10000).forEach(i -> finalString.append(i).append("Hello"));
    }

    @Override
    public HelloReply sayHello(HelloRequest request) {
        LOGGER.info("Received sayHello request: {}", request.getName());
        return toReply(finalString + " " + request.getName());
    }

    @Override
    public CompletableFuture<String> sayHelloAsync(String request) {
        LOGGER.info("Received sayHelloAsync request: {}", request);
        return CompletableFuture.supplyAsync(() -> "Hello " + request);
    }

    @Override
    public void sayHelloServerStream(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        LOGGER.info("Received sayHelloServerStream request");
        for (int i = 1; i < 6; i++) {
            LOGGER.info("sayHelloServerStream onNext: {} {} times", request.getName(), i);
            responseObserver.onNext(toReply(finalString + " " + request.getName()));
        }
        LOGGER.info("sayHelloServerStream onCompleted");
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<HelloRequest> sayHelloBiStream(StreamObserver<HelloReply> responseObserver) {
        LOGGER.info("Received sayHelloBiStream request");
        return new StreamObserver<>() {
            @Override
            public void onNext(HelloRequest request) {
                LOGGER.info("sayHelloBiStream onNext: {}", request.getName());
                responseObserver.onNext(toReply("Hello " + request.getName()));
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.error("sayHelloBiStream onError", throwable);
            }

            @Override
            public void onCompleted() {
                LOGGER.info("sayHelloBiStream onCompleted");
                responseObserver.onCompleted();
            }
        };
    }

    private static HelloReply toReply(String message) {
        return HelloReply.newBuilder().setMessage(message).build();
    }
}
