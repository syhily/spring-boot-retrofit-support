package com.oneapm.touch.retrofit.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneapm.touch.retrofit.autoconfigure.RetrofitProperties.Connection;
import com.oneapm.touch.retrofit.autoconfigure.RetrofitProperties.Log;
import com.oneapm.touch.retrofit.boot.context.LocalRetrofitContext;
import com.oneapm.touch.retrofit.boot.context.RetrofitContext;
import com.oneapm.touch.retrofit.boot.intercepts.HttpLoggingInterceptor;
import com.oneapm.touch.retrofit.boot.intercepts.HttpLoggingInterceptor.ContentLevel;
import com.oneapm.touch.retrofit.boot.intercepts.RetryInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
@ConditionalOnClass(Retrofit.class)
@EnableConfigurationProperties(RetrofitProperties.class)
public class RetrofitAutoConfiguration {

    private final List<Converter.Factory> converterFactories;

    private final OkHttpClient okHttpClient;

    private final RetrofitProperties retrofitProperties;

    @Autowired
    public RetrofitAutoConfiguration(List<Converter.Factory> converterFactories, OkHttpClient okHttpClient,
                                     RetrofitProperties retrofitProperties) {
        this.converterFactories = converterFactories;
        this.okHttpClient = okHttpClient;
        this.retrofitProperties = retrofitProperties;
        checkConfiguredUrl(this.retrofitProperties);
    }

    @Slf4j
    @Configuration
    @ConditionalOnClass(OkHttpClient.class)
    public static class OkHttpClientConfiguration {

        /**
         * Mark this as a bean for user to easy monitor the connection status
         */
        @Bean
        @ConditionalOnMissingBean
        public ConnectionPool connectionPool(RetrofitProperties properties) {
            Connection connection = properties.getConnection();
            return new ConnectionPool(connection.getMaxIdleConnections(), connection.getKeepAliveDuration(), MINUTES);
        }

        @Bean
        @ConditionalOnMissingBean
        public OkHttpClient okHttpClient(RetrofitProperties properties, ConnectionPool connectionPool) {
            Connection connection = properties.getConnection();
            Log logConf = properties.getLog();

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(connection.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(connection.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(connection.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .connectionPool(connectionPool);

            if (connection.getRetryTimes() != null && connection.getRetryTimes() > 0) {
                builder.addInterceptor(new RetryInterceptor(connection.getRetryTimes()));
            }
            if (logConf.getEnabled() != null && logConf.getEnabled()) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(log, logConf.getLevel());
                loggingInterceptor.setContentLevel(logConf.getContent());
                builder.addInterceptor(loggingInterceptor);
            }
            return builder.build();
        }
    }

    @Configuration
    @ConditionalOnClass(JacksonConverterFactory.class)
    public static class JacksonConverterFactoryConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ObjectMapper mapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
            return mapper;
        }

        @Bean
        @ConditionalOnMissingBean
        public JacksonConverterFactory jacksonConverterFactory(ObjectMapper mapper) {
            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            return JacksonConverterFactory.create(mapper);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public RetrofitContext retrofitContext() {
        Builder builder = new Builder().validateEagerly(true);
        converterFactories.forEach(builder::addConverterFactory);
        if (okHttpClient != null) {
            builder.client(okHttpClient);
        }
        RetrofitContext context = new LocalRetrofitContext();
        retrofitProperties.getEndpoints()
            .forEach(endPoint -> context.register(endPoint.getIdentity(), builder.baseUrl(endPoint.getBaseUrl()).build()));
        return context;
    }

    /**
     * Check the configured url format is valid by using {@code new URI()}
     */
    private void checkConfiguredUrl(RetrofitProperties properties) {
        properties.getEndpoints().stream()
            .map(RetrofitProperties.EndPoint::getBaseUrl)
            .forEach(url -> {
                Assert.isTrue(ResourceUtils.isUrl(url), url + " is not a valid url");
                if (!url.endsWith("/")) {
                    log.warn("The [{}] didn't end with \"/\". This means a relative base url, end with / would be better.", url);
                }
            });
    }
}
