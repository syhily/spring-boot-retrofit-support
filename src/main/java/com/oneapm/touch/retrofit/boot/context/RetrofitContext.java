package com.oneapm.touch.retrofit.boot.context;

import retrofit2.Retrofit;

import java.util.Optional;

/**
 * The k v store for retrofit instance, because the retrofit instance is immutable,
 * and we couldn't get some useful identify from it's public method.
 * <p>
 * In order to support multiply base url endpoint instance, we must create and store them separately.
 */
public interface RetrofitContext {

    /**
     * Register the given retrofit to specified identity,if the context already hold the given identity,
     * we would return the old retrofit instance
     */
    Retrofit register(String identity, Retrofit retrofit);

    /**
     * remove the given retrofit from context
     *
     * @return true for succeed in remove, false for the given retrofit identity doesn't existed
     */
    boolean unregister(String identity);

    Optional<Retrofit> getRetrofit(String identity);

    boolean hasRetrofit(String identity);

    boolean empty();
}
