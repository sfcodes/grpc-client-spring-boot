package codes.sf.springboot.grpc.client.context;

import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AssignableTypeFilter;

import static org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_BY_TYPE;

/**
 * A bean definition scanner that detects <b>gRPC stubs</b> on the classpath, and
 * registers them as bean definitions with a given registry ({@code BeanFactory}
 * or {@code ApplicationContext}).
 *
 * <p>By default it detects all classes on the classpath that extend
 * {@link io.grpc.stub.AbstractStub}
 *
 * @author Semyon Fishman
 * @see io.grpc.stub.AbstractStub
 * @see <a href="https://grpc.io/docs/tutorials/basic/java.html">
 * gRPC Basics - Java</a>
 * @see <a href="https://grpc.io/docs/reference/java/generated-code.html">
 * gRPC Java Generated Code Reference</a>
 * @since 0.0.1
 */
public class GrpcStubScanner extends ClassPathBeanDefinitionScanner {


    public GrpcStubScanner(BeanDefinitionRegistry registry) {
        super(registry, true);
    }

    @Override
    protected void registerDefaultFilters() {
        addIncludeFilter(new AssignableTypeFilter(io.grpc.stub.AbstractStub.class));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {

        String beanName = definitionHolder.getBeanName();
        ScannedGenericBeanDefinition definition = (ScannedGenericBeanDefinition) definitionHolder.getBeanDefinition();

        String factoryClassName = definition.getMetadata().getEnclosingClassName();
        Class<?> stubClass = resolveBeanClass(beanName, definition);

        definition.setBeanClass(GrpcStubFactoryBean.class);
        definition.setInstanceSupplier(() -> new GrpcStubFactoryBean(factoryClassName, stubClass));
        definition.setAutowireMode(AUTOWIRE_BY_TYPE);

        super.registerBeanDefinition(definitionHolder, registry);
    }

    private Class<?> resolveBeanClass(String beanName, ScannedGenericBeanDefinition definition) {
        try {
            return definition.resolveBeanClass(getResourceLoader().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new CannotLoadBeanClassException(definition.getResourceDescription(), beanName, definition.getBeanClassName(), e);
        }
    }
}
