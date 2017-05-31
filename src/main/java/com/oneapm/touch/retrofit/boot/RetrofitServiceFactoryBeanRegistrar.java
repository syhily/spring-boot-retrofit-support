package com.oneapm.touch.retrofit.boot;

import com.oneapm.touch.retrofit.boot.annotation.RetrofitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Bean definition registrar responsible for registering Retrofit specific bean definitions.
 * <p>
 * Currently registering instantiation aware bean post processor adapter responsible for instantiating Retrofit
 * services and registering individual Retrofit service interfaces as bean definitions so that they can
 * instantiated by the post processor adapter.
 */
@Slf4j
public class RetrofitServiceFactoryBeanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(RetrofitServiceBeanPostProcessorAdapter.BEAN_NAME)) {
            registry.registerBeanDefinition(RetrofitServiceBeanPostProcessorAdapter.BEAN_NAME,
                new RootBeanDefinition(RetrofitServiceBeanPostProcessorAdapter.class));
        }
        doRegisterRetrofitServiceBeanDefinitions(annotationMetadata, registry);
    }

    /**
     * Scans for interfaces annotated with {@link RetrofitService} from the packages defined by
     * {@link RetrofitServiceScan}.
     *
     * @param annotationMetadata annotation metadata of the importing class
     * @param registry           current bean definition registry
     */
    private void doRegisterRetrofitServiceBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        RetrofitServiceComponentProvider provider = RetrofitServiceComponentProvider.getInstance();

        // Find packages to scan for Retrofit services.
        Set<String> packagesToScan = getPackagesToScan(annotationMetadata);

        for (String packageToScan : packagesToScan) {
            log.debug("Trying to find candidates from package {}", packageToScan);

            Set<BeanDefinition> candidates = provider.findCandidateComponents(packageToScan);

            if (!candidates.isEmpty()) {
                processCandidates(candidates, registry);
            }
        }
    }

    /**
     * Processes the given set of bean definitions and registers them to the bean definition registry
     * to be further processed by {@link RetrofitServiceBeanPostProcessorAdapter}.
     *
     * @param candidates the candidates to register.
     * @param registry   the bean registry.
     */
    private void processCandidates(Set<BeanDefinition> candidates, BeanDefinitionRegistry registry) {
        log.debug("Found {} Retrofit Service candidate(s)", candidates.size());

        for (BeanDefinition beanDefinition : candidates) {
            String beanName = generateBeanName(beanDefinition);

            log.debug("Processing candidate class {} with bean name {}",
                beanDefinition.getBeanClassName(),
                beanName);

            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }

    /**
     * Inspects the packages to be scanned for {@link RetrofitService} interfaces from the {@link RetrofitServiceScan}
     * import annotation.
     *
     * @param metadata the annotation metadata.
     * @return a set of packages to be scanned for {@link RetrofitService} candidates.
     */
    private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes
            .fromMap(metadata.getAnnotationAttributes(RetrofitServiceScan.class.getName()));

        String[] value = attributes.getStringArray("value");
        String[] basePackages = attributes.getStringArray("basePackages");
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");

        if (!ObjectUtils.isEmpty(value)) {
            Assert.state(ObjectUtils.isEmpty(basePackages),
                "@RetrofitServiceScan basePackages and value attributes are mutually exclusive");
        }

        Set<String> packagesToScan = new LinkedHashSet<String>();
        packagesToScan.addAll(Arrays.asList(value));
        packagesToScan.addAll(Arrays.asList(basePackages));

        for (Class<?> basePackageClass : basePackageClasses) {
            packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
        }

        if (packagesToScan.isEmpty()) {
            return Collections.singleton(ClassUtils.getPackageName(metadata.getClassName()));
        }

        return packagesToScan;
    }

    /**
     * Constructs a bean name for the given bean definition.
     *
     * @param beanDefinition the bean definition from which to extract the information in order to construct a name.
     * @return a bean name.
     */
    private String generateBeanName(BeanDefinition beanDefinition) {
        // Try obtaining the client specified bean name if available in the annotated interface
        try {
            Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
            RetrofitService retrofitService = beanClass.getAnnotation(RetrofitService.class);

            if (retrofitService != null && StringUtils.hasText(retrofitService.name())) {
                return retrofitService.name();
            }

            // Support @Qualifier retrofitService
            Qualifier qualifier = beanClass.getAnnotation(Qualifier.class);
            if (qualifier != null && !"".equals(qualifier.value())) {
                return qualifier.value();
            }

            // Reduce the conflict of same endpoint class name, use full package class name instead
            // So we wouldn't prefer to use AnnotationBeanNameGenerator
            return beanClass.getName();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot obtain bean name for Retrofit service interface", e);
        }
    }
}
