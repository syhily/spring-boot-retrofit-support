package com.oneapm.touch.retrofit.boot;

import com.oneapm.touch.retrofit.autoconfigure.RetrofitProperties;
import retrofit2.Retrofit;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for constructing {@link Retrofit} service instances.
 */
public class RetrofitServiceFactory {
    private final Retrofit.Builder retrofitBuilder;
    private final ConcurrentHashMap<String, Retrofit> retrofitMap;
    private final RetrofitProperties properties;

    public RetrofitServiceFactory(Retrofit.Builder retrofitBuilder, RetrofitProperties properties) {
        this.retrofitBuilder = retrofitBuilder;
        this.properties = properties;
        this.retrofitMap = new ConcurrentHashMap<>();
    }

    public <T> T createServiceInstance(Class<T> serviceClass, String retrofitId) {
        Retrofit retrofit = getConfiguredRetrofit(retrofitId);
        return retrofit.create(serviceClass);
    }

    private Retrofit getConfiguredRetrofit(String beanId) {
        return retrofitMap.computeIfAbsent(beanId, key -> {
            RetrofitProperties.EndPoint endPoint = properties.getEndPoint(beanId);
            return retrofitBuilder.baseUrl(endPoint.getBaseUrl()).build();
        });
    }
}
