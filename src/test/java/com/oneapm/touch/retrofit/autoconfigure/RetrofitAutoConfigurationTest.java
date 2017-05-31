package com.oneapm.touch.retrofit.autoconfigure;

import com.oneapm.touch.retrofit.boot.RetrofitServiceScan;
import com.oneapm.touch.retrofit.boot.annotation.RetrofitService;
import lombok.Data;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RetrofitAutoConfiguration}
 */
public class RetrofitAutoConfigurationTest {

    @Autowired
    private AnnotationConfigApplicationContext context;

    @RetrofitService("ai")
    public interface MyService {
        @GET("/hello")
        Call<Hello> sayHello();

        @GET("/hello-observable-scalar")
        Call<String> toHelloObservable();
    }

    @RetrofitService(value = "ai")
    public interface MyService2 {
        @GET("/hello")
        Call<Hello> sayHello();

        @GET("/hello-observable-scalar")
        Call<String> toHelloObservable();
    }

    @RetrofitService(name = MyCustomBeanNameService.BEAN_NAME, retrofit = "bi")
    public interface MyCustomBeanNameService {
        String BEAN_NAME = "myBeanName";

        @GET("/hello")
        Call<Hello> sayHello();
    }

    @Configuration
    @RetrofitServiceScan
    public static class RetrofitTestConfiguration {
        // To enable service scanning
    }

    @Before
    public void setup() {
        loadContext();
    }

    @After
    public void teardown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void testRetrofitAutoConfigured() {
        Retrofit.Builder builder = context.getBean(Retrofit.Builder.class);
        assertThat(builder).isNotNull();
    }

    @Test
    public void testRetrofitAutoConfiguredWithConverters() {
        Retrofit.Builder builder = context.getBean(Retrofit.Builder.class);
        Retrofit retrofit = builder.build();

        // Assert that we have exactly the converter factories that are auto-configured
        List<Converter.Factory> converterFactories = retrofit.converterFactories();

        // Retrofit internally adds BuildInConverters
        assertThat(converterFactories).hasSize(2).hasAtLeastOneElementOfType(JacksonConverterFactory.class);
    }

    @Test
    public void testMyServiceAutoConfigured() {
        MyService myService = context.getBean(MyService.class);
        assertThat(myService).isNotNull();
    }

    @Test
    public void testCustomizingRetrofitServiceBeanName() {
        MyCustomBeanNameService myCustomBeanNameService = context.getBean(MyCustomBeanNameService.BEAN_NAME, MyCustomBeanNameService.class);
        assertThat(myCustomBeanNameService).isNotNull();
    }

    private void loadContext() {
        context = new AnnotationConfigApplicationContext();
        EnvironmentTestUtils.addEnvironment(context, "retrofit.enable=true",
            "retrofit.endpoints[0].identity=ai", "retrofit.endpoints[0].baseUrl=http://127.0.0.1:10010",
            "retrofit.endpoints[1].identity=bi", "retrofit.endpoints[1].baseUrl=http://127.0.0.1:10011",
            "retrofit.connection.timeout=5000");
        context.register(RetrofitAutoConfiguration.class, RetrofitTestConfiguration.class);
        context.refresh();
    }

    @Data
    private static class Hello {
        private String message;
    }
}
