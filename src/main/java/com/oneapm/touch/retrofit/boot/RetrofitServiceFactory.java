package com.oneapm.touch.retrofit.boot;

import com.oneapm.touch.retrofit.boot.context.RetrofitContext;
import retrofit2.Retrofit;

/**
 * Factory for constructing {@link Retrofit} service instances.
 */
class RetrofitServiceFactory {
    private final RetrofitContext retrofitContext;

    RetrofitServiceFactory(RetrofitContext retrofitContext) {
        this.retrofitContext = retrofitContext;
    }

    <T> T createServiceInstance(Class<T> serviceClass, String retrofitId) {
        Retrofit retrofit = getConfiguredRetrofit(retrofitId);
        return retrofit.create(serviceClass);
    }

    private Retrofit getConfiguredRetrofit(String beanId) {
        return retrofitContext.getRetrofit(beanId)
            .orElseThrow(() -> new RuntimeException("Cannot obtain [" + beanId + "] Retrofit in your application configuration file."));
    }
}
