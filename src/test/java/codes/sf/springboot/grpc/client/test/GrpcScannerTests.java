package codes.sf.springboot.grpc.client.test;

import codes.sf.springboot.grpc.client.GrpcStubScan;
import codes.sf.springboot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import codes.sf.springboot.grpc.client.context.GrpcStubScanner;
import org.junit.Test;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ScopedProxyMode;

import static io.grpc.examples.generated.GreeterGrpc.*;
import static org.assertj.core.api.Assertions.assertThat;


public class GrpcScannerTests {

    @Test
    public void emptyScanPackagesPropertyTest() {

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GrpcClientAutoConfiguration.class))
                .withPropertyValues("grpc.client.scanPackages=")
                .run(this::assertDoesNotHaveGreeterStubs);
    }

    @Test
    public void springBootApplicationAnnotationTest() {

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GrpcClientAutoConfiguration.class))
                .withUserConfiguration(io.grpc.examples.SpringBootApplicationAnnotationConfiguration.class)
                .run(this::assertHasGreeterStubs);
    }

    @Test
    public void componentScanAnnotationTest() {

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GrpcClientAutoConfiguration.class))
                .withUserConfiguration(io.grpc.examples.ComponentScanAnnotationConfiguration.class)
                .run(this::assertHasGreeterStubs);
    }

    //
    @Test
    public void grpcStubScanAnnotationTest() {

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GrpcClientAutoConfiguration.class))
                .withUserConfiguration(io.grpc.examples.GrpcStubScanAnnotationConfiguration.class)
                .run(this::assertHasGreeterStubs);
    }

    @Test
    public void basePackageClassesAnnotationTest() {

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GrpcClientAutoConfiguration.class))
                .withUserConfiguration(BasePackageClassesAnnotationConfiguration.class)
                .run(this::assertHasGreeterStubs);
    }

    @GrpcStubScan(basePackageClasses = io.grpc.examples.generated.GreeterGrpc.class)
    static class BasePackageClassesAnnotationConfiguration {
    }

    @Test
    public void basePackagesAnnotationTest() {

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GrpcClientAutoConfiguration.class))
                .withUserConfiguration(BasePackagesAnnotationConfiguration.class)
                .run(this::assertHasGreeterStubs);
    }

    @GrpcStubScan(basePackages = "io.grpc.examples")
    static class BasePackagesAnnotationConfiguration {
    }

    @Test
    public void beanNameAnnotationTest() {

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GrpcClientAutoConfiguration.class))
                .withUserConfiguration(BeanNameGeneratorAnnotationConfiguration.class)
                .run(context -> {
                    this.assertHasGreeterStubs(context);
                    assertThat(context).hasBean("Stub1");
                    assertThat(context).hasBean("Stub2");
                    assertThat(context).hasBean("Stub3");
                });
    }

    @GrpcStubScan(basePackages = "io.grpc.examples",
            nameGenerator = TestBeanNameGenerator.class)
    static class BeanNameGeneratorAnnotationConfiguration {
    }

    static class TestBeanNameGenerator extends DefaultBeanNameGenerator {

        private int i = 0;

        @Override
        public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
            i++;
            return "Stub" + i;
        }
    }

    @Test
    public void scopedProxyAnnotationTest() {

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GrpcClientAutoConfiguration.class))
                .withUserConfiguration(ScopedProxyAnnotationConfiguration.class)
                .run(context -> {
                    // TODO: how do I test this other than just setting it?
                });
    }

    @GrpcStubScan(basePackages = "io.grpc.examples",
            scopedProxy = ScopedProxyMode.TARGET_CLASS)
    static class ScopedProxyAnnotationConfiguration {
    }

    //////

    private void assertHasGreeterStubs(AssertableApplicationContext context) {

        // io.grpc.examples stubs should be found by default
        assertThat(context).hasSingleBean(GreeterStub.class);
        assertThat(context).hasSingleBean(GreeterBlockingStub.class);
        assertThat(context).hasSingleBean(GreeterFutureStub.class);
    }

    private void assertDoesNotHaveGreeterStubs(AssertableApplicationContext context) {

        // io.grpc.examples stubs should be found by default
        assertThat(context).doesNotHaveBean(GreeterStub.class);
        assertThat(context).doesNotHaveBean(GreeterBlockingStub.class);
        assertThat(context).doesNotHaveBean(GreeterFutureStub.class);
    }

    @Test(expected = CannotLoadBeanClassException.class)
    public void cannotLoadBeanClassExceptionTest() {

        BeanDefinitionRegistry registry = new DefaultListableBeanFactory();
        GrpcStubScanner scanner = new BrokenGrpcStubScanner(registry);
        scanner.scan("io.grpc.examples");
    }

    static class BrokenGrpcStubScanner extends GrpcStubScanner {
        public BrokenGrpcStubScanner(BeanDefinitionRegistry registry) {
            super(registry);
        }

        @Override
        protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
            definitionHolder.getBeanDefinition().setBeanClassName("invalid.class.name");
            super.registerBeanDefinition(definitionHolder, registry);
        }
    }


}
