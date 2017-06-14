package com.oneapm.touch.retrofit.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Boot Retrofit autoconfigure properties
 */
@Data
@ConfigurationProperties(prefix = "retrofit")
public class RetrofitProperties {

    private Connection connection = new Connection();

    private List<EndPoint> endpoints = new ArrayList<>();

    @Data
    public static class EndPoint {

        private String identity;

        private String baseUrl;
    }

    @Data
    public static class Connection {

        private Long readTimeout = 5000L;

        private Long writeTimeout = 5000L;

        private Long connectTimeout = 5000L;

        private Integer maxIdleConnections = 5;

        private Integer keepAliveDuration = 5;
    }
}
