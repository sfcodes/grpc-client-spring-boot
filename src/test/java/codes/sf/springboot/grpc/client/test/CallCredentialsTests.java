package codes.sf.springboot.grpc.client.test;

import io.grpc.Attributes;
import io.grpc.CallCredentials;
import io.grpc.MethodDescriptor;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

import static codes.sf.springboot.grpc.client.test.GrpcTestUtils.test;
import static io.grpc.examples.generated.GreeterGrpc.GreeterStub;
import static org.junit.Assert.assertSame;

public class CallCredentialsTests {

    @Test
    public void callCredentialsTest() {
        test(runner -> runner
                .withUserConfiguration(CallCredentialsConfiguration.class)
                .run(context -> {
                    CallCredentials callCredentials = context.getBean(CallCredentials.class);
                    GreeterStub stub = context.getBean(GreeterStub.class);

                    assertSame("CallCredentials",
                            callCredentials, stub.getCallOptions().getCredentials());
                })
        );
    }

    @Configuration
    static class CallCredentialsConfiguration {
        @Bean
        public CallCredentials callCredentials() {
            return new CallCredentials() {
                @Override
                public void applyRequestMetadata(MethodDescriptor<?, ?> method, Attributes attrs, Executor appExecutor, MetadataApplier applier) {
                    // MOCK
                }

                @Override
                public void thisUsesUnstableApi() {
                    // MOCK
                }
            };
        }
    }
}
