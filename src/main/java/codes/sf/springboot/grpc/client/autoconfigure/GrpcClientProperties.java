package codes.sf.springboot.grpc.client.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * gRPC client properties.
 *
 * @author Semyon Fishman
 * @since 0.0.1
 */
@ConfigurationProperties(prefix = GrpcClientProperties.PREFIX)
public class GrpcClientProperties {

    public static final String PREFIX = "grpc.client";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 6565;
    public static final String DEFAULT_TARGET = DEFAULT_HOST + ":" + DEFAULT_PORT;

    /**
     * Sets base package to scan for gRPC stubs . This property is ignored
     * if &#64;GrpcStubScan annotation is present.
     */
    private String[] scanPackages;

    /**
     * Sets target address for gRPC stubs' channel.
     */
    private String target;

    /**
     * Configures the gRPC stubs to use the executor found in the
     * ApplicationContext, instead of the default one.
     */
    private boolean springexecutor = false;

    /**
     * Sets the compressor name to use for gRPC calls.
     */
    private String compression;

    /**
     * Limits the maximum acceptable message size from remote peers.
     */
    private Integer maxInboundMessageSize;

    /**
     * Limits the maximum acceptable message size to send remote peers.
     */
    private Integer maxOutboundMessageSize;

    public String[] getScanPackages() {
        return scanPackages;
    }

    public void setScanPackages(String[] scanPackages) {
        this.scanPackages = scanPackages;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isSpringexecutor() {
        return springexecutor;
    }

    public void setSpringexecutor(boolean springexecutor) {
        this.springexecutor = springexecutor;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public Integer getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public void setMaxInboundMessageSize(Integer maxInboundMessageSize) {
        this.maxInboundMessageSize = maxInboundMessageSize;
    }

    public Integer getMaxOutboundMessageSize() {
        return maxOutboundMessageSize;
    }

    public void setMaxOutboundMessageSize(Integer maxOutboundMessageSize) {
        this.maxOutboundMessageSize = maxOutboundMessageSize;
    }
}
