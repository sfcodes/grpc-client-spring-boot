package codes.sf.springboot.grpc.client.test;

import io.grpc.examples.generated.GreeterGrpc;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static codes.sf.springboot.grpc.client.test.GrpcTestUtils.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CallOptionsTests {

    @Test
    public void springexecutorOptionTest() {
        test(runner -> runner
                .withUserConfiguration(AsyncConfiguration.class)
                .withPropertyValues("grpc.client.springexecutor=true")
                .run(context -> {
                    Executor executor = context.getBean(Executor.class);
                    GreeterGrpc.GreeterStub stub
                            = context.getBean(GreeterGrpc.GreeterStub.class);

                    assertSame("grpc.client.springexecutor= property doesn't work.",
                            executor, stub.getCallOptions().getExecutor());
                })
        );
    }

    @EnableAsync
    @Configuration
    static class AsyncConfiguration {
        @Bean
        public TaskExecutor taskExecutor() {
            return new ConcurrentTaskExecutor(
                    Executors.newSingleThreadExecutor());
        }
    }

    @Test
    public void compressionOptionTest() {
        test(runner -> runner
                .withPropertyValues("grpc.client.compression=gzip")
                .run(context -> {
                    GreeterGrpc.GreeterStub stub
                            = context.getBean(GreeterGrpc.GreeterStub.class);

                    assertEquals("grpc.client.compression= property doesn't work.",
                            "gzip", stub.getCallOptions().getCompressor());
                })
        );
    }

    @Test
    public void maxInboundMessageSizeOptionTest() {
        test(runner -> runner
                .withPropertyValues("grpc.client.maxInboundMessageSize=123")
                .run(context -> {
                    GreeterGrpc.GreeterStub stub
                            = context.getBean(GreeterGrpc.GreeterStub.class);

                    assertEquals("grpc.client.maxInboundMessageSize= property doesn't work.",
                            Integer.valueOf(123), stub.getCallOptions().getMaxInboundMessageSize());
                })
        );
    }

    @Test
    public void maxOutboundMessageSizeOptionTest() {
        test(runner -> runner
                .withPropertyValues("grpc.client.maxOutboundMessageSize=567")
                .run(context -> {
                    GreeterGrpc.GreeterStub stub
                            = context.getBean(GreeterGrpc.GreeterStub.class);

                    assertEquals("grpc.client.maxOutboundMessageSize= property doesn't work.",
                            Integer.valueOf(567), stub.getCallOptions().getMaxOutboundMessageSize());
                })
        );
    }
}
