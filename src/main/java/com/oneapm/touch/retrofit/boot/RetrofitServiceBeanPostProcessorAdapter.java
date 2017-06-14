package com.oneapm.touch.retrofit.boot;

import com.oneapm.touch.retrofit.autoconfigure.RetrofitProperties;
import com.oneapm.touch.retrofit.boot.annotation.RetrofitService;
import com.oneapm.touch.retrofit.boot.context.RetrofitContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.Assert;
import retrofit2.Retrofit;

/**
 * Instantiation aware bean post processor adapter to instantiate the bean interfaces marked with {@link RetrofitService}
 * annotation.
 * <p>
 * The beans can't be annotated in the bean definition phase as the {@link Retrofit} bean is needed in order
 * to construct the actual service instances. In addition, the service specific configurations are accessed
 * through {@link RetrofitProperties} {@link org.springframework.boot.context.properties.ConfigurationProperties}
 *
 * @author troinine
 */
public class RetrofitServiceBeanPostProcessorAdapter extends InstantiationAwareBeanPostProcessorAdapter implements BeanFactoryAware, PriorityOrdered {

    /**
     * The name of this bean.
     */
    static final String BEAN_NAME = "retrofitServiceBeanPostProcessorAdapter";

    private BeanFactory beanFactory;
    private RetrofitServiceFactory retrofitServiceFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE - 1;
    }

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        Object ret = null;

        if (beanClass.isAnnotationPresent(RetrofitService.class)) {
            RetrofitService annotation = beanClass.getAnnotation(RetrofitService.class);
            String retrofitId = "".equals(annotation.retrofit()) ? annotation.value() : annotation.retrofit();
            ret = getRetrofitServiceFactory().createServiceInstance(beanClass, retrofitId);
        }

        return ret;
    }

    /**
     * Lazy-inits the associated Retrofit service factory because the needed dependencies are available after
     * the needed bean dependencies have been created by the {@link BeanFactory}.
     *
     * @return {@link RetrofitServiceFactory} ready to construct service instances.
     */
    private RetrofitServiceFactory getRetrofitServiceFactory() {
        Assert.notNull(beanFactory, "BeanFactory may not be null");

        if (retrofitServiceFactory == null) {
            RetrofitContext context = beanFactory.getBean(RetrofitContext.class);

            retrofitServiceFactory = new RetrofitServiceFactory(context);
        }

        return retrofitServiceFactory;
    }
}
