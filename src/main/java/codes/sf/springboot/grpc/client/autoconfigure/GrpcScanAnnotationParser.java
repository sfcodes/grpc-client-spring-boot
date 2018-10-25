package codes.sf.springboot.grpc.client.autoconfigure;

import codes.sf.springboot.grpc.client.context.GrpcStubScanner;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * Parser for {@link codes.sf.springboot.grpc.client.GrpcStubScan @GrpcStubScan}
 * and {@link org.springframework.context.annotation.ComponentScan @ComponentScan}
 * for configuring gRPC stub scanning.
 *
 * @author Semyon Fishman
 * @see GrpcStubScanner
 * @since 0.0.1
 */
class GrpcScanAnnotationParser {

    private static final String DECLARING_CLASS_KEY =
            GrpcScanAnnotationParser.class.getCanonicalName() + "_DECLARING_CLASS_KEY";

    private final Environment environment;
    private final BeanDefinitionRegistry registry;
    private final GrpcStubScanner scanner;

    public GrpcScanAnnotationParser(Environment environment, BeanDefinitionRegistry registry, GrpcStubScanner scanner) {
        this.environment = environment;
        this.registry = registry;
        this.scanner = scanner;
    }

    public String[] parse(Class<? extends Annotation> annotationClass) {

        AnnotationAttributes annotation = findInRegistry(annotationClass);
        if (annotation == null)
            return null;

        // Generator Class
        Class<? extends BeanNameGenerator> generatorClass = annotation.getClass("nameGenerator");
        if (BeanNameGenerator.class != generatorClass)
            scanner.setBeanNameGenerator(BeanUtils.instantiateClass(generatorClass));

        // Scope
        ScopedProxyMode scopedProxyMode = annotation.getEnum("scopedProxy");
        if (scopedProxyMode != ScopedProxyMode.DEFAULT) {
            scanner.setScopedProxyMode(scopedProxyMode);
        } else {
            Class<? extends ScopeMetadataResolver> resolverClass = annotation.getClass("scopeResolver");
            scanner.setScopeMetadataResolver(BeanUtils.instantiateClass(resolverClass));
        }

        // Resource Pattern
        scanner.setResourcePattern(annotation.getString("resourcePattern"));

        Set<String> basePackages = new LinkedHashSet<>();
        String[] basePackagesArray = annotation.getStringArray("basePackages");
        for (String pkg : basePackagesArray) {
            String[] tokenized = StringUtils.tokenizeToStringArray(this.environment.resolvePlaceholders(pkg),
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            Collections.addAll(basePackages, tokenized);
        }
        for (Class<?> clazz : annotation.getClassArray("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }
        if (basePackages.isEmpty()) {
            String declaringClass = (String) annotation.get(DECLARING_CLASS_KEY);
            basePackages.add(ClassUtils.getPackageName(declaringClass));
        }

        scanner.addExcludeFilter(new AbstractTypeHierarchyTraversingFilter(false, false) {
            @Override
            protected boolean matchClassName(String className) {
                String declaringClass = (String) annotation.get(DECLARING_CLASS_KEY);
                return declaringClass.equals(className);
            }
        });

        return basePackages.toArray(new String[basePackages.size()]);
    }

    private AnnotationAttributes findInRegistry(Class<? extends Annotation> annotationClass) {
        String[] candidateNames = registry.getBeanDefinitionNames();
        for (String beanName : candidateNames) {
            BeanDefinition beanDef = registry.getBeanDefinition(beanName);
            if (beanDef instanceof AnnotatedBeanDefinition) {
                AnnotationMetadata metadata = ((AnnotatedBeanDefinition) beanDef).getMetadata();
                if (metadata.isAnnotated(annotationClass.getName())) {
                    AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(annotationClass.getName(), false));
                    attributes.put(DECLARING_CLASS_KEY, beanDef.getBeanClassName());
                    return attributes;
                }
            }
        }
        return null;
    }
}
