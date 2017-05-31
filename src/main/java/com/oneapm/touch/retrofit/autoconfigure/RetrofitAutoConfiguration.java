package com.oneapm.touch.retrofit.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
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

    @Autowired
    public RetrofitAutoConfiguration(List<Converter.Factory> converterFactories, OkHttpClient okHttpClient,
                                     RetrofitProperties retrofitProperties) {
        this.converterFactories = converterFactories;
        this.okHttpClient = okHttpClient;
        checkConfiguredUrl(retrofitProperties);
    }

    @Configuration
    @ConditionalOnClass(OkHttpClient.class)
    public static class OkHttpClientConfiguration {

        /**
         * Mark this as a bean for user to easy monitor the connection status
         */
        @Bean
        @ConditionalOnMissingBean
        public ConnectionPool connectionPool(RetrofitProperties properties) {
            RetrofitProperties.Connection connection = properties.getConnection();
            return new ConnectionPool(connection.getMaxIdleConnections(), connection.getKeepAliveDuration(), MINUTES);
        }

        @Bean
        @ConditionalOnMissingBean
        public OkHttpClient okHttpClient(RetrofitProperties properties, ConnectionPool connectionPool) {
            return new OkHttpClient.Builder()
                .readTimeout(properties.getConnection().getTimeout(), TimeUnit.MILLISECONDS)
                .connectionPool(connectionPool)
                .build();
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
            return JacksonConverterFactory.create(mapper);
        }
    }

    /**
     * Check the configured url format is valid by using {@code new URI()}
     */
    private void checkConfiguredUrl(RetrofitProperties properties) {
        properties.getEndpoints().stream()
            .map(RetrofitProperties.EndPoint::getBaseUrl)
            .forEach(url -> {
                Assert.isTrue(ResourceUtils.isUrl(url), url + " is not a valid url");
                if (url.endsWith("/")) {
                    log.warn("The {} end with would require absolute path, remove it would be better.", url);
                }
            });
    }

    @Bean
    @ConditionalOnMissingBean
    public Retrofit.Builder retrofit() {
        Retrofit.Builder builder = new Retrofit.Builder();
        converterFactories.forEach(builder::addConverterFactory);
        if (okHttpClient != null) {
            builder.client(okHttpClient);
        }
        return builder;
    }
}
