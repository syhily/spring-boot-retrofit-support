package com.oneapm.touch.retrofit.boot.annotation;

import com.oneapm.touch.retrofit.boot.RetrofitServiceScan;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates an interface as Retrofit service.
 * <p>
 * Use this annotation to qualify a Retrofit annotated interface for auto-detection and automatic
 * instantiation.
 *
 * @see RetrofitServiceScan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface RetrofitService {

    /**
     * Defines the name of the service bean when registered to the underlying context. If left unspecified
     * the name of the service bean is generated using {@link org.springframework.beans.factory.annotation.Qualifier},
     * If no Qualifier annotation, we would use full class name instead.
     *
     * @return the name of the bean.
     */
    String name() default "";

    /**
     * Alias for the {@link #retrofit()} attribute. Allows for more concise annotation
     * declarations e.g.: {@code @RetrofitService("ai")} instead of
     * {@code @RetrofitService(retrofit="ai")}.
     *
     * @return the specified retrofit instance to build endpoint
     */
    String value() default "default";

    /**
     * Defines the name of retrofit should be used in building the service endpoint
     * eg. ai, bi, mi or cep {@link #value()} is an alias for (and mutually exclusive with) this attribute.
     */
    String retrofit() default "";
}
