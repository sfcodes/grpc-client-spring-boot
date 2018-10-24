package codes.sf.springboot.grpc.client;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractStub;

/**
 * A strategy interface for resolving {@linkplain Channel Channels} for gRPC stubs.
 *
 * <p>GrpcChannelSource is usually where your <em>service discovery</em> logic
 * goes. It is the place where you map the client stubs to their server
 * implementations.
 *
 * <p>Auto configuration will autodetect the GrpcChannelSource bean and
 * use it to resolve channels for new gRPC stub instances. In other words
 * you may register your GrpcChannelSource as a simple {@code @Bean} in
 * your {@code @Configuration} classes.
 *
 * <p>Note that there must be <b>at most one</b> GrpcChannelSource in the
 * ApplicationContext, otherwise you'll get an error.
 *
 * <p>If no GrpcChannelSource is found in the ApplicationContext, auto
 * configuration will
 * <ul>
 * <li>
 * first look for a {@link Channel} in the ApplicationContext to use instead;
 * </li>
 * <li>
 * if no channel bean is found, it will configure a new
 * {@linkplain ManagedChannelBuilder#usePlaintext() plaintext} one
 * with target from environment property {@code grpc.client.target};
 * </li>
 * <li>
 * finally if no property is found, it will default to creating a new
 * plaintext channel bean with target "{@code localhost:6565}"
 * </li>
 * </ul>
 *
 * <p>If you're creating a GrpcChannelSource that returns a constant channel,
 * consider using convenience function {@link #of(Channel) GrpcChannelSource.of(Channel)}
 *
 * @author Semyon Fishman
 * @since 0.0.1
 */
@FunctionalInterface
public interface GrpcChannelSource {

    /**
     * Resolve channel for stub type.
     *
     * @param stubClass the stub type
     * @return resolved channel, may not be {@code null}
     */
    Channel resolve(Class<? extends AbstractStub<?>> stubClass);

    /**
     * Convenience method for build GrpcChannelSource that returns
     * a constant channel instance.
     *
     * @param channel the constant channel instance to return
     * @return newly constructed GrpcChannelSource
     */
    static GrpcChannelSource of(Channel channel) {
        return stubClass -> channel;
    }
}
