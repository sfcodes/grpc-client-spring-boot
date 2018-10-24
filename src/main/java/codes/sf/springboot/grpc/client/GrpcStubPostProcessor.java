package codes.sf.springboot.grpc.client;

import io.grpc.stub.AbstractStub;

/**
 * Factory hook that allows for custom modification of new gRPC stub instances.
 *
 * <p>Auto configuration will autodetect GrpcStubPostProcessor beans and
 * automatically apply them to new gRPC stub instances. In other words you
 * may register these GrpcStubPostProcessor's as simple {@code @Bean}'s in
 * your {@code @Configuration} classes.
 *
 * <p>An GrpcStubPostProcessor can generically declare the stub type that it is
 * interested in, in which case he processor will only be invoked on the
 * matching stub instances.
 *
 * <p>GrpcStubPostProcessor maybe be ordered using either the
 * {@linkplain org.springframework.core.Ordered ordered interface} or
 * {@link org.springframework.core.annotation.Order @Order annotation}. Note
 * that you may also apply {@code @Order} to your {@code @Bean} methods.
 *
 * @author Semyon Fishman
 * @see codes.sf.springboot.grpc.client.stubpostprocess.GenericGrpcStubPostProcessor GenericGrpcStubPostProcessor
 * @since 0.0.1
 */
@FunctionalInterface
public interface GrpcStubPostProcessor<S extends AbstractStub<S>> {

    /**
     * Apply this GrpcStubPostProcessor to the given new gRPC stub instance.
     *
     * @param stub the new gRPC stub instance
     * @return the gRPC stub instance to use, either the original or
     * wrapped one; may not be @{code null}
     */
    S postProcess(S stub);
}
