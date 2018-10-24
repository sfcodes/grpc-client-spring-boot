package codes.sf.springboot.grpc.client.test;

import io.grpc.examples.generated.GreeterGrpc;
import io.grpc.examples.generated.GreeterOuterClass;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

import static io.grpc.examples.generated.GreeterOuterClass.HelloReply;

public class GreeterService extends GreeterGrpc.GreeterImplBase {

    private static final Logger LOGGER = Logger.getLogger(GreeterService.class.getName());

    @Override
    public void sayHello(GreeterOuterClass.HelloRequest request, StreamObserver<GreeterOuterClass.HelloReply> responseObserver) {

        LOGGER.info("gRPC request{" + request + "}");

        HelloReply reply = HelloReply.newBuilder()
                .setMessage(expectedSayHello(request.getName()))
                .build();
        responseObserver.onNext(reply);

        LOGGER.info("gRPC response{" + reply + "}");

        responseObserver.onCompleted();
    }

    /**
     * EXPOSED STATICALLY FOR TESTING PURPOSES ONLY!
     *
     * @param name
     * @return
     */
    public static String expectedSayHello(String name) {
        return "Hello " + name;
    }
}
