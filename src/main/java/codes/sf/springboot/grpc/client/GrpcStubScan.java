package codes.sf.springboot.grpc.client;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Configures gRPC stubs scanning.
 *
 * <p>Either {@link #basePackageClasses} or {@link #basePackages} may be
 * specified to define specific packages to scan. If specific packages are not
 * defined, scanning will occur from the package of the class that declares
 * this annotation.
 *
 * <p>If this annotation is missing, auto configuration will fallback on
 * environment property {@code grpc.client.scanPackages}. If the property
 * is missing as well, auto configuration will finally fall back on
 * the values in {@link ComponentScan @ComponentScan}.
 *
 * @author Semyon Fishman
 * @see <a href="https://grpc.io/docs/tutorials/basic/java.html">
 * gRPC Basics - Java</a>
 * @see <a href="https://grpc.io/docs/reference/java/generated-code.html">
 * gRPC Java Generated Code Reference</a>
 * @since 0.0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface GrpcStubScan {


    /**
     * Alias for {@link #basePackages}.
     * <p>Allows for more concise annotation declarations if no other attributes
     * are needed &mdash; for example, {@code @GrpcStubScan("org.my.pkg")}
     * instead of {@code @GrpcStubScan(basePackages = "org.my.pkg")}.
     *
     * @return packages to scan for gRPC stubs
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * Base packages to scan for gRPC stubs
     * <p>{@link #value} is an alias for (and mutually exclusive with) this
     * attribute.
     * <p>Use {@link #basePackageClasses} for a type-safe alternative to
     * String-based package names.
     *
     * @return packages to scan for gRPC stubs
     */
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages} for specifying the packages
     * to scan for gRPC stubs. The package of each class specified will be scanned.
     * <p>Consider using this type-safe alternative with the generated stubs'
     * enclosing class. For example
     * {@code @GrpcStubScan(basePackageClasses = "org.my.pkg.grpc.MyServiceGrpc.class")}
     *
     * @return packages to scan for gRPC stubs
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * The {@link BeanNameGenerator} class to be used for naming detected gRPC stubs
     * within the Spring container.
     * <p>The default value of the {@link BeanNameGenerator} interface itself indicates
     * that the scanner used to process this {@code @GrpcStubScan} annotation should
     * use its inherited bean name generator, e.g. the default
     * {@link AnnotationBeanNameGenerator} or any custom instance supplied to the
     * application context at bootstrap time.
     *
     * @return class to be used for naming detected gRPC stub beans
     * @see AnnotationConfigApplicationContext#setBeanNameGenerator(BeanNameGenerator)
     */
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

    /**
     * The {@link ScopeMetadataResolver} to be used for resolving the scope of detected gRPC stubs.
     *
     * @return ScopeMetadataResolver class
     */
    Class<? extends ScopeMetadataResolver> scopeResolver() default AnnotationScopeMetadataResolver.class;

    /**
     * Indicates whether proxies should be generated for detected gRPC stubs, which may be
     * necessary when using scopes in a proxy-style fashion.
     * <p>The default is defer to the default behavior of the gRPC stub scanner used to
     * execute the actual scan.
     * <p>Note that setting this attribute overrides any value set for {@link #scopeResolver}.
     *
     * @return scoped proxy mode to use with stubs
     * @see codes.sf.springboot.grpc.client.context.GrpcStubScanner#setScopedProxyMode(ScopedProxyMode) GrpcStubScanner.setScopedProxyMode(ScopedProxyMode)
     */
    ScopedProxyMode scopedProxy() default ScopedProxyMode.DEFAULT;

    /**
     * Controls the class files eligible for gRPC stubs detection.
     *
     * @return pattern of gRPC classes stubs
     */
    String resourcePattern() default "**/*.class"; // Copied from ClassPathScanningCandidateComponentProvider.DEFAULT_RESOURCE_PATTERN;
}
