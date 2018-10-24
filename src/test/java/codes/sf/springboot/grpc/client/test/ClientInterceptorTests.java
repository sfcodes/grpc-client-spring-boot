package codes.sf.springboot.grpc.client.test;

import io.grpc.*;
import io.grpc.examples.generated.GreeterGrpc;
import io.grpc.examples.generated.GreeterOuterClass;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;

import static codes.sf.springboot.grpc.client.test.GrpcTestUtils.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientInterceptorTests {

    private static List<String> testlog = new ArrayList<>();

    @Test
    public void interceptorsOptionTest() {

        testlog.clear(); // make sure it's empty

        test(runner -> runner
                .withUserConfiguration(ClientInterceptorsConfiguration.class)
                .run(context -> {
                    GreeterGrpc.GreeterFutureStub stub
                            = context.getBean(GreeterGrpc.GreeterFutureStub.class);

                    stub.sayHello(GreeterOuterClass.HelloRequest.newBuilder().build());

                    // First check that interceptors were called in the first place
                    assertTrue("clientInterceptorA did not run!",
                            testlog.contains("clientInterceptorA"));
                    assertTrue("clientInterceptorB did not run!",
                            testlog.contains("clientInterceptorB"));
                    assertTrue("clientInterceptorC did not run!",
                            testlog.contains("clientInterceptorC"));

                    // Then check that interceptors were called in correct order
                    assertEquals("clientInterceptorA run out of order!",
                            "clientInterceptorA", testlog.get(2));
                    assertEquals("clientInterceptorB run out of order!",
                            "clientInterceptorB", testlog.get(1));
                    assertEquals("clientInterceptorC run out of order!",
                            "clientInterceptorC", testlog.get(0));
                })
        );
    }

    @Configuration
    static class ClientInterceptorsConfiguration {

        @Order(1)
        @Bean
        public ClientInterceptor clientInterceptorA() {
            return new ClientInterceptor() {
                @Override
                public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                        MethodDescriptor<ReqT, RespT> method,
                        CallOptions callOptions,
                        Channel next) {

                    testlog.add("clientInterceptorA");

                    return next.newCall(method, callOptions);
                }
            };
        }

        @Order(2)
        @Bean
        public ClientInterceptor clientInterceptorB() {
            return new ClientInterceptor() {
                @Override
                public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                        MethodDescriptor<ReqT, RespT> method,
                        CallOptions callOptions,
                        Channel next) {

                    testlog.add("clientInterceptorB");

                    return next.newCall(method, callOptions);
                }
            };
        }

        @Order(3)
        @Bean
        public ClientInterceptor clientInterceptorC() {
            return new ClientInterceptor() {
                @Override
                public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                        MethodDescriptor<ReqT, RespT> method,
                        CallOptions callOptions,
                        Channel next) {

                    testlog.add("clientInterceptorC");

                    return next.newCall(method, callOptions);
                }
            };
        }

    }

}
