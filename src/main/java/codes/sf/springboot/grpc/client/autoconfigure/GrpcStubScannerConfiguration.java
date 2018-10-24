package codes.sf.springboot.grpc.client.autoconfigure;

import codes.sf.springboot.grpc.client.GrpcStubScan;
import codes.sf.springboot.grpc.client.context.GrpcStubScanner;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
class GrpcStubScannerConfiguration implements EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public BeanFactoryPostProcessor grpcStubScannerBeanFactoryPostProcessor() {
        return beanFactory -> {

            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

            GrpcStubScanner scanner = new GrpcStubScanner(registry);
            GrpcScanAnnotationParser annotationParser = new GrpcScanAnnotationParser(environment, registry, scanner);

            String[] scanPackages = annotationParser.parse(GrpcStubScan.class);
            if (scanPackages == null) scanPackages = getEnvironmentProperty();
            if (scanPackages == null) scanPackages = annotationParser.parse(ComponentScan.class);

            if (scanPackages.length != 0)
                scanner.scan(scanPackages);
        };
    }

    private String[] getEnvironmentProperty() {
        return environment.getProperty(GrpcClientProperties.PREFIX + ".scanPackages", String[].class);
    }


}
