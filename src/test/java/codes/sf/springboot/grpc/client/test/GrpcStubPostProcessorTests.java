package codes.sf.springboot.grpc.client.test;

import codes.sf.springboot.grpc.client.GrpcStubPostProcessor;
import codes.sf.springboot.grpc.client.stubpostprocess.GenericGrpcStubPostProcessor;
import io.grpc.CallOptions;
import io.grpc.stub.AbstractStub;
import org.junit.Test;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;

import static codes.sf.springboot.grpc.client.test.GrpcTestUtils.test;
import static io.grpc.examples.generated.GreeterGrpc.*;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GrpcStubPostProcessorTests {

    private static final CallOptions.Key<String> regularProcessorKey
            = CallOptions.Key.create("regularProcessorKey");

    private static final CallOptions.Key<String> blockingStubOnlyProcessorKey
            = CallOptions.Key.create("blockingStubOnlyProcessorKey");

    private static final CallOptions.Key<String> proxiedProcessorKey
            = CallOptions.Key.create("proxiedProcessorKey");

    private static final CallOptions.Key<String> genericProcessorKey
            = CallOptions.Key.create("genericProcessorKey");

    private static final List<String> testlog = new ArrayList<>();

    private static void log(Class aClass, String message) {
        testlog.add(aClass.getCanonicalName() + message);
    }

    private static List<String> getLogFor(Class aClass) {
        return testlog.stream()
                .filter(s -> s.startsWith(aClass.getCanonicalName()))
                .map(s -> s.replaceFirst(quote(aClass.getCanonicalName()), ""))
                .collect(toList());
    }

    @Test
    public void grpcStubPostProcessorTest() {

        testlog.clear(); // clear before test

        test(runner -> runner
                .withUserConfiguration(GrpcStubPostProcessorConfiguration.class)
                .run(context -> {

                    //
                    GreeterStub stub = context.getBean(GreeterStub.class);
                    GreeterFutureStub futureStub = context.getBean(GreeterFutureStub.class);
                    GreeterBlockingStub blockingStub = context.getBean(GreeterBlockingStub.class);

                    // This checks if the order was right
                    List<String> stubLog = getLogFor(GreeterStub.class);
                    assertEquals(2, stubLog.size());
                    assertEquals("proxiedProcessored", stubLog.get(0));
                    assertEquals("regularProcessored", stubLog.get(1));

                    //
                    List<String> futureStubLog = getLogFor(GreeterFutureStub.class);
                    assertEquals(3, futureStubLog.size());
                    assertEquals("proxiedProcessored", futureStubLog.get(0));
                    assertEquals("regularProcessored", futureStubLog.get(1));
                    assertEquals("genericGrpcStubPostProcessored", futureStubLog.get(2));

                    //
                    List<String> blockingStubLog = getLogFor(GreeterBlockingStub.class);
                    assertEquals(3, blockingStubLog.size());
                    assertEquals("proxiedProcessored", blockingStubLog.get(0));
                    assertEquals("blockingStubOnlyProcessored", blockingStubLog.get(1));
                    assertEquals("regularProcessored", blockingStubLog.get(2));

                    //
                    assertEquals("regularProcessorValue", stub.getCallOptions().getOption(regularProcessorKey));
                    assertEquals("proxiedProcessorValue", stub.getCallOptions().getOption(proxiedProcessorKey));
                    assertNull(stub.getCallOptions().getOption(blockingStubOnlyProcessorKey));
                    assertNull(stub.getCallOptions().getOption(genericProcessorKey));

                    //
                    assertEquals("regularProcessorValue", futureStub.getCallOptions().getOption(regularProcessorKey));
                    assertEquals("proxiedProcessorValue", futureStub.getCallOptions().getOption(proxiedProcessorKey));
                    assertNull(futureStub.getCallOptions().getOption(blockingStubOnlyProcessorKey));
                    assertEquals("genericProcessorValue", futureStub.getCallOptions().getOption(genericProcessorKey));

                    //
                    assertEquals("regularProcessorValue", blockingStub.getCallOptions().getOption(regularProcessorKey));
                    assertEquals("proxiedProcessorValue", blockingStub.getCallOptions().getOption(proxiedProcessorKey));
                    assertEquals("blockingStubOnlyProcessorValue", blockingStub.getCallOptions().getOption(blockingStubOnlyProcessorKey));
                    assertNull(blockingStub.getCallOptions().getOption(genericProcessorKey));

                })
        );
    }

    @Configuration
    static class GrpcStubPostProcessorConfiguration {


        @Order(3)
        @Bean
        public GrpcStubPostProcessor regularProcessor() {
            return stub -> {
                log(stub.getClass(), "regularProcessored");
                return stub.withOption(regularProcessorKey, "regularProcessorValue");
            };
        }

        @Order(1)
        @Bean
        public GrpcStubPostProcessor proxiedProcessor() {
            DefaultAopProxyFactory proxyFactory = new DefaultAopProxyFactory();
            AdvisedSupport advisedSupport = new AdvisedSupport();
            advisedSupport.setTarget((GrpcStubPostProcessor) stub -> {
                log(stub.getClass(), "proxiedProcessored");
                return stub.withOption(proxiedProcessorKey, "proxiedProcessorValue");
            });
            advisedSupport.setProxyTargetClass(true);
            AopProxy aopProxy = proxyFactory.createAopProxy(advisedSupport);
            return (GrpcStubPostProcessor) aopProxy.getProxy();
        }

        @Order(2)
        @Bean
        public GrpcStubPostProcessor<GreeterBlockingStub> blockingStubOnlyProcessor() {
            // Due to known bug in Spring, this implementation cannot be lambda because
            // it'll lose it's generic type, see https://jira.spring.io/browse/SPR-13698
            //noinspection Convert2Lambda
            return new GrpcStubPostProcessor<GreeterBlockingStub>() {
                @Override
                public GreeterBlockingStub postProcess(GreeterBlockingStub stub) {
                    log(stub.getClass(), "blockingStubOnlyProcessored");
                    return stub.withOption(blockingStubOnlyProcessorKey, "blockingStubOnlyProcessorValue");
                }
            };
        }

        @Order(4)
        @Bean
        public GenericGrpcStubPostProcessor genericGrpcStubPostProcessor() {
            return new GenericGrpcStubPostProcessor() {
                @Override
                public boolean supportsStubType(Class<? extends AbstractStub> stubClass) {
                    return GreeterFutureStub.class.isAssignableFrom(stubClass);
                }

                @Override
                public AbstractStub postProcess(AbstractStub stub) {
                    log(stub.getClass(), "genericGrpcStubPostProcessored");
                    return stub.withOption(genericProcessorKey, "genericProcessorValue");
                }
            };
        }

    }
}
