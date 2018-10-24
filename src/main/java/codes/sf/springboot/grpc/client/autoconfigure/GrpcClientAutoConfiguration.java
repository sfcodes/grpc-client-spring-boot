package codes.sf.springboot.grpc.client.autoconfigure;

import codes.sf.springboot.grpc.client.GrpcChannelSource;
import codes.sf.springboot.grpc.client.GrpcStubPostProcessor;
import codes.sf.springboot.grpc.client.stubpostprocess.GenericGrpcStubPostProcessor;
import codes.sf.springboot.grpc.client.stubpostprocess.GenericGrpcStubPostProcessorAdapter;
import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import static java.util.stream.Collectors.toList;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for gRPC client stubs.
 *
 * <p>This auto-configuration will scan the
 * {@linkplain codes.sf.springboot.grpc.client.context.GrpcStubScanner
 * classpath for gRPC stubs} and instantiate them with a default or
 * user-configured {@link Channel}.
 *
 * @author Semyon Fishman
 * @see GrpcClientProperties
 * @since 0.0.1
 */
@EnableConfigurationProperties(GrpcClientProperties.class)
@Configuration
@Import(GrpcStubScannerConfiguration.class)
public class GrpcClientAutoConfiguration {

    public static final String GENERIC_GRPC_STUB_POST_PROCESSORS_BEAN_NAME =
            "GrpcClientAutoConfiguration_genericGrpcStubPostProcessors";

    private List<GrpcStubPostProcessor> postProcessors;
    private Channel channel;

    private final GrpcClientProperties properties;

    GrpcClientAutoConfiguration(GrpcClientProperties properties) {
        this.properties = properties;
    }

    @Autowired(required = false)
    void setGrpcStubPostProcessors(List<GrpcStubPostProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    @Autowired(required = false)
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Bean(name = GENERIC_GRPC_STUB_POST_PROCESSORS_BEAN_NAME)
    public List<GenericGrpcStubPostProcessor> genericGrpcStubPostProcessors() {
        return Optional.ofNullable(this.postProcessors)
                .orElse(Collections.emptyList())
                .stream()
                .map(processor -> (processor instanceof GenericGrpcStubPostProcessor) ?
                        (GenericGrpcStubPostProcessor) processor
                        : new GenericGrpcStubPostProcessorAdapter(processor)
                )
                .collect(toList());
    }

    @Bean
    @ConditionalOnMissingBean(GrpcChannelSource.class)
    public GrpcChannelSource channelSource() {

        Channel channel;

        if (this.channel != null)
            channel = this.channel;
        else {
            String target = properties.getTarget();
            if (target == null || target.isEmpty())
                target = GrpcClientProperties.DEFAULT_TARGET;

            channel = ManagedChannelBuilder
                    .forTarget(target)
                    .usePlaintext()
                    .build();
        }

        return GrpcChannelSource.of(channel);
    }

    @Bean
    @ConditionalOnProperty(prefix = GrpcClientProperties.PREFIX, name = "springexecutor")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public GrpcStubPostProcessor springExecutorGrpcStubPostProcessor(Executor executor) {
        return stub -> stub.withExecutor(executor);
    }

    @Bean
    @ConditionalOnProperty(prefix = GrpcClientProperties.PREFIX, name = "compression")
    public GrpcStubPostProcessor compressionGrpcStubPostProcessor() {
        return stub -> stub.withCompression(properties.getCompression());
    }

    @Bean
    @ConditionalOnBean(ClientInterceptor.class)
    public GrpcStubPostProcessor interceptorsGrpcStubPostProcessor(List<ClientInterceptor> interceptors) {
        return stub -> {
            for (ClientInterceptor interceptor : interceptors)
                stub = stub.withInterceptors(interceptor);
            return stub;
        };
    }

    @Bean
    @ConditionalOnBean(CallCredentials.class)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public GrpcStubPostProcessor callCredentialsGrpcStubPostProcessor(CallCredentials credentials) {
        return stub -> stub.withCallCredentials(credentials);
    }

    @Bean
    @ConditionalOnProperty(prefix = GrpcClientProperties.PREFIX, name = "maxInboundMessageSize")
    public GrpcStubPostProcessor maxInboundMessageSizeGrpcStubPostProcessor() {
        return stub -> stub.withMaxInboundMessageSize(properties.getMaxInboundMessageSize());
    }

    @Bean
    @ConditionalOnProperty(prefix = GrpcClientProperties.PREFIX, name = "maxOutboundMessageSize")
    public GrpcStubPostProcessor maxOutboundMessageSizeGrpcStubPostProcessor() {
        return stub -> stub.withMaxOutboundMessageSize(properties.getMaxOutboundMessageSize());
    }

}
