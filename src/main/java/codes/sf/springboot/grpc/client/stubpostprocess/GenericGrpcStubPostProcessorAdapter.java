package codes.sf.springboot.grpc.client.stubpostprocess;

import codes.sf.springboot.grpc.client.GrpcStubPostProcessor;
import io.grpc.stub.AbstractStub;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import static org.springframework.core.ResolvableType.forClass;

/**
 * {@link GenericGrpcStubPostProcessor} adapter that determines supported stub types
 * through introspecting the generically declared type of the delegate listener.
 *
 * @author Semyon Fishman
 * @since 0.0.1
 */
public class GenericGrpcStubPostProcessorAdapter implements GenericGrpcStubPostProcessor {

    private final GrpcStubPostProcessor delegate;
    private final ResolvableType declaredStubType;

    public GenericGrpcStubPostProcessorAdapter(GrpcStubPostProcessor delegate) {
        Assert.notNull(delegate, "Delegate GrpcStubPostProcessor must not be null");
        this.delegate = delegate;
        this.declaredStubType = resolveDeclaredStubType(delegate);
    }

    @Override
    public boolean supportsStubType(Class<? extends AbstractStub> stubClass) {
        ResolvableType stubType = forClass(stubClass);
        return this.declaredStubType.isAssignableFrom(stubType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AbstractStub postProcess(AbstractStub stub) {
        return this.delegate.postProcess(stub);
    }

    // https://jira.spring.io/browse/SPR-13698
    private static ResolvableType resolveDeclaredStubType(GrpcStubPostProcessor processor) {
        ResolvableType declaredStubType = resolveDeclaredStubType(processor.getClass());
        if (declaredStubType.isAssignableFrom(AbstractStub.class)) {
            Class<?> targetClass = AopUtils.getTargetClass(processor);
            if (targetClass != processor.getClass()) {
                declaredStubType = resolveDeclaredStubType(targetClass);
            }
        }
        return declaredStubType;
    }

    private static ResolvableType resolveDeclaredStubType(Class<?> processorType) {
        ResolvableType resolvableType = forClass(processorType).as(GrpcStubPostProcessor.class);
        return resolvableType.getGeneric();
    }
}
