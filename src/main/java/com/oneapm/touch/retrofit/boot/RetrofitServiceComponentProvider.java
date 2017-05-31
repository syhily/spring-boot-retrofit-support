package com.oneapm.touch.retrofit.boot;

import com.oneapm.touch.retrofit.boot.annotation.RetrofitService;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Custom classpath scanner which includes interfaces that have been annotated with {@link RetrofitService}.
 * <p>
 * Since Retrofit only supports interfaces, all other types are ignored.
 */
public class RetrofitServiceComponentProvider extends ClassPathScanningCandidateComponentProvider {

    private RetrofitServiceComponentProvider() {
        super(false);
        addIncludeFilter(new AnnotationTypeFilter(RetrofitService.class, true, true));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface();
    }

    static RetrofitServiceComponentProvider getInstance() {
        return new RetrofitServiceComponentProvider();
    }
}
