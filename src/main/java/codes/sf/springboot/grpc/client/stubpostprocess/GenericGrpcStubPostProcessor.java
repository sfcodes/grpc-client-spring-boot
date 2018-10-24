package codes.sf.springboot.grpc.client.stubpostprocess;

import codes.sf.springboot.grpc.client.GrpcStubPostProcessor;
import io.grpc.stub.AbstractStub;

/**
 * Extended variant of {@link GrpcStubPostProcessor} interface that allows
 * implementing explicit logic deciding when to apply the processor.
 *
 * @author Semyon Fishman
 * @since 0.0.1
 */
public interface GenericGrpcStubPostProcessor extends GrpcStubPostProcessor {

    /**
     * Determine whether this stub post processor should apply to given stub type.
     *
     * @param stubClass the stub type (never {@code null})
     * @return {@code true} is this processor should be applied to the given
     * stub type; {@code false} otherwise
     */
    boolean supportsStubType(Class<? extends AbstractStub> stubClass);
}
