package codes.sf.springboot.grpc.client.test;

import codes.sf.springboot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import com.github.javafaker.Faker;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.generated.GreeterGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static codes.sf.springboot.grpc.client.test.GreeterService.expectedSayHello;
import static io.grpc.examples.generated.GreeterOuterClass.HelloReply;
import static io.grpc.examples.generated.GreeterOuterClass.HelloRequest.newBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;

abstract class GrpcTestUtils {

    private static final Faker faker = new Faker(new Random(1));

    private GrpcTestUtils() {
        // static class; prevent instantiation
    }

    private static ApplicationContextRunner applicationContextRunner() {
        return new ApplicationContextRunner()
                .withPropertyValues("grpc.client.scanPackages=io.grpc.examples")
                .withConfiguration(AutoConfigurations.of(GrpcClientAutoConfiguration.class));
    }

    public static void test(Consumer<ApplicationContextRunner> runnerConsumer) {
        runnerConsumer.accept(applicationContextRunner());
    }

    public static void testWithServer(int serverPort, Consumer<ApplicationContextRunner> runnerConsumer) {

        Server server = ServerBuilder
                .forPort(serverPort)
                .addService(new GreeterService())
                .build();

        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to start gRPC server for test.", e);
        }

        try {
            test(runnerConsumer);
        } finally {
            server.shutdownNow();
        }

        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void assertStubsWork(AssertableApplicationContext context) {
        assertStubWorks(context);
        assertBlockingStubWorks(context);
        assertFutureStubWorks(context);
    }

    public static void assertBlockingStubWorks(AssertableApplicationContext context) {

        assertThat(context).hasSingleBean(GreeterGrpc.GreeterBlockingStub.class);

        GreeterGrpc.GreeterBlockingStub stub
                = context.getBean(GreeterGrpc.GreeterBlockingStub.class);

        String name = faker.name().fullName();

        HelloReply reply = stub.sayHello(newBuilder().setName(name).build());

        assertEquals(expectedSayHello(name), reply.getMessage());
    }

    private static void assertStubWorks(AssertableApplicationContext context) {

        assertThat(context).hasSingleBean(GreeterGrpc.GreeterStub.class);

        GreeterGrpc.GreeterStub stub
                = context.getBean(GreeterGrpc.GreeterStub.class);

        String name = faker.name().fullName();

        CompletableFuture<HelloReply> future = new CompletableFuture<>();
        stub.sayHello(newBuilder().setName(name).build(), new StreamObserver<HelloReply>() {

            private HelloReply value;

            @Override
            public void onNext(HelloReply value) {
                this.value = value;
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                future.complete(value);
            }
        });

        try {
            assertEquals(expectedSayHello(name), future.get().getMessage());
        } catch (InterruptedException e) {
            fail(e.getMessage(), e);
        } catch (ExecutionException e) {
            fail(e.getCause().getMessage(), e.getCause());
        }
    }

    private static void assertFutureStubWorks(AssertableApplicationContext context) {

        assertThat(context).hasSingleBean(GreeterGrpc.GreeterStub.class);

        GreeterGrpc.GreeterFutureStub stub
                = context.getBean(GreeterGrpc.GreeterFutureStub.class);

        String name = faker.name().fullName();


        ListenableFuture<HelloReply> future = stub.sayHello(newBuilder().setName(name).build());
        try {
            assertEquals(expectedSayHello(name), future.get().getMessage());
        } catch (InterruptedException e) {
            fail(e.getMessage(), e);
        } catch (ExecutionException e) {
            fail(e.getCause().getMessage(), e.getCause());
        }
    }
}
