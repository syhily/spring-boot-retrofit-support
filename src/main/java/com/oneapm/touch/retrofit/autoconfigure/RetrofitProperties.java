package com.oneapm.touch.retrofit.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

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

    /**
     * Return the required endpoint by using the given identity
     */
    public EndPoint getEndPoint(String id) {
        Assert.notNull(id, "The query retrofit end point id shouldn't be empty");
        return endpoints.stream().filter(point -> id.equals(point.getIdentity())).findAny()
            .orElseThrow(() -> new RuntimeException("Cannot obtain [" + id + "] Retrofit in your application configuration file."));
    }

    @Data
    public static class EndPoint {

        private String identity;

        private String baseUrl;
    }

    @Data
    public static class Connection {

        private Long timeout = 5000L;

        private Integer maxIdleConnections = 5;

        private Integer keepAliveDuration = 5;
    }
}
