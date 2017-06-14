package com.oneapm.touch.retrofit.boot.context;

import retrofit2.Retrofit;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LocalRetrofitContext extends ConcurrentHashMap<String, Retrofit> implements RetrofitContext {
    private static final long serialVersionUID = -5865286831705661141L;

    @Override
    public Retrofit register(String identity, Retrofit retrofit) {
        return put(identity, retrofit);
    }

    @Override
    public boolean unregister(String identity) {
        return remove(identity) != null;
    }

    @Override
    public Optional<Retrofit> getRetrofit(String identity) {
        return Optional.ofNullable(get(identity));
    }

    @Override
    public boolean hasRetrofit(String identity) {
        return containsKey(identity);
    }

    @Override
    public boolean empty() {
        clear();
        return true;
    }
}
