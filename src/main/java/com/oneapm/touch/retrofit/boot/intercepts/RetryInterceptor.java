package com.oneapm.touch.retrofit.boot.intercepts;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * The retry count limit intercept, would retry the net request until success or reach the limited times
 *
 * @link https://stackoverflow.com/questions/37883418/does-okhttpclient-have-a-max-retry-count
 */
@Slf4j
@AllArgsConstructor
public class RetryInterceptor implements Interceptor {

    private final Integer retryTimes;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = doRequest(chain, request);
        int tryCount = 0;
        while (response == null && tryCount < retryTimes) {
            Request newRequest = request.newBuilder().build(); // Avoid the cache
            response = doRequest(chain, newRequest);
            tryCount++;
            log.warn("Request failed, retry to acquire a new connection, {} in {} times", tryCount, retryTimes);
        }
        if (response == null) { // Important ,should throw an exception here
            throw new IOException();
        }
        return response;
    }

    private Response doRequest(Chain chain, Request request) {
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            log.error("", e);
        }
        return response;
    }
}
