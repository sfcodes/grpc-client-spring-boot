package codes.sf.springboot.grpc.client.test;

import codes.sf.springboot.grpc.client.GrpcChannelSource;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.generated.GreeterGrpc;
import org.junit.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static codes.sf.springboot.grpc.client.autoconfigure.GrpcClientProperties.DEFAULT_PORT;
import static codes.sf.springboot.grpc.client.test.GrpcTestUtils.testWithServer;

public class ChannelTests {

    @Test
    public void unConfiguredChannel() {
        testWithServer(DEFAULT_PORT, runner -> {
            runner.run(GrpcTestUtils::assertStubsWork);
        });
    }

    @Test
    public void emptyConfiguredChannel() {
        testWithServer(DEFAULT_PORT, runner -> {
            runner.withPropertyValues("grpc.client.target=")
                    .run(GrpcTestUtils::assertStubsWork);
        });
    }

    @Test
    public void propertiesConfiguredChannel() {
        testWithServer(2002, runner -> {
            runner.withPropertyValues("grpc.client.target=localhost:2002")
                    .run(GrpcTestUtils::assertStubsWork);
        });
    }

    @Test
    public void beanConfiguredChannel() {
        testWithServer(2003, runner -> {
            runner.withUserConfiguration(Channel2003Configuration.class)
                    .run(GrpcTestUtils::assertStubsWork);
        });
    }

    @Configuration
    static class Channel2003Configuration {
        @Bean
        public Channel channel2003() {
            Channel channel = ManagedChannelBuilder
                    .forAddress("localhost", 2003)
                    .usePlaintext()
                    .build();
            return channel;
        }
    }

    @Test
    public void beanConfiguredChannelSource() {
        testWithServer(2005, runner -> {
            runner.withUserConfiguration(GrpcChannelSource2005Configuration.class)
                    .run(GrpcTestUtils::assertStubsWork);
        });
    }

    @Configuration
    static class GrpcChannelSource2005Configuration {

        private final Channel channel = ManagedChannelBuilder
                .forAddress("localhost", 2005)
                .usePlaintext()
                .build();

        @Bean
        public GrpcChannelSource grpcChannelSource2004() {
            return GrpcChannelSource.of(channel);
        }
    }

    @Test(expected = UnsatisfiedDependencyException.class)
    public void multipleBeanConfiguredChannelSources() throws Throwable {
        try {
            testWithServer(2005, runner -> {
                runner.withUserConfiguration(MultipleGrpcChannelSourcesConfiguration.class)
                        .run(context -> {
                            context.getBean(GreeterGrpc.GreeterStub.class);
                        });
            });
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof UnsatisfiedDependencyException)
                throw e.getCause();
            else
                throw e;
        }
    }

    @Configuration
    static class MultipleGrpcChannelSourcesConfiguration {

        private final Channel channel = ManagedChannelBuilder
                .forAddress("localhost", 2005)
                .usePlaintext()
                .build();

        @Bean
        public GrpcChannelSource grpcChannelSource1() {
            return GrpcChannelSource.of(channel);
        }

        @Bean
        public GrpcChannelSource grpcChannelSource2() {
            return GrpcChannelSource.of(channel);
        }
    }
}
