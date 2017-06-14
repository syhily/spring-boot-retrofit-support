## Retrofit Springboot Auto Configuration

This project is mainly for integration with other business system. We use springboot to
get better configuration and dependence inversion.

Retrofit 2 is just a simple abstraction for http request. The backend of its to perform http
request is mainly depends on `okhttp3`. We also support async http request out of the box.

### Architecture

```
com.oneapm.touch.retrofit
 ├── autoconfigure: Retrofit configuration module and its configuration properties definition
 └── boot: the bean registry factory for building retrofit endpoint interface instance
```

### Dependency

Find the [latest version](http://nexus.oneapm.me:8081/nexus/#nexus-search;gav~com.oneapm.touch.retrofit~retrofit-spring-boot-starter~~~)
and grad it via Maven:

```xml
<dependency>
  <groupId>com.oneapm.touch.retrofit</groupId>
  <artifactId>retrofit-spring-boot-starter</artifactId>
  <version>${latest.starter.version}</version>
</dependency>
```

or Gradle:

```
compile "com.oneapm.touch.retrofit:retrofit-spring-boot-starter:latest.version"
```

### Detailed Usage in Springboot Application

In your main Spring Boot application, you need to add the annotation `@RetrofitServiceScan` on the configuration class
(aka. the class which has spring annotation `@Configuration`) or spring boot's bootstrap class (the class annotated with `@SpringBootApplication`)
to enable the auto bean generation for all the customized retrofit interface. e.g.:

```java
@SpringBootApplication
@RetrofitServiceScan
public class MainApplication  {

    public static void main(final String ... args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
```

All the customize retrofit interface should be annotated with `@RetrofitService` annotation,
that would make these interface could be registed our own bean creation process. e.g.:

```java
@RetrofitService("ai")
public interface AiAgentEndpoint {

    @GET("ai/v5/apps/{appId}/tiers/{tierId}/agents")
    Call<List<AiAgentRes>> getAgents(@Path("appId") long appId, @Path("tierId") long tierId);
}
```

`@RetrofitService` have three properties, the `value` or `retrofit` stand for the identity of the retrofit configuration
that your would like to use (It would be clarify in configuration file part). `name` attribute means the spring's bean name.
If no attributes provided, we would pick the interface's class name as the bean name and the `default` retrofit configuration.

In you `application.yml` set the configuration needed to send http request.

**YAML**

```yaml
# The http configuration part for integration with other system
retrofit:
  connection:
    readTimeout: 10000
    writeTimeout: 10000
    connectTimeout: 10000
    maxIdleConnections: 5 # The maximum number of idle connections for each address.
    keepAliveDuration: 5 # The time (minutes) to live for each idle connections.
    retryTimes: 5

  # identity: current available
  # baseUrl: the base part of business system url, would be changed by nginx location, "/" is not required to be the end of url
  endpoints:
    - identity: default
      baseUrl: http://127.0.0.1:${random.int(10000,15000)}
    - identity: ai
      baseUrl: http://127.0.0.1:${random.int(10000,15000)}
    - identity: mi
      baseUrl: http://127.0.0.1:${random.int(10000,15000)}
    - identity: cep
      baseUrl: http://127.0.0.1:${random.int(10000,15000)}
```

**Java Properties**

```properties
retrofit.timeout=5000
retrofit.connection.read-timeout=10000
retrofit.connection.write-timeout=10000
retrofit.connection.connect-timeout=10000
retrofit.connection.max-idle-connections=5
retrofit.connection.keep-alive-duration=5
retrofit.connection.retry-times=5
# The list of outer system to communicate with should have a index form like 
# retrofit.endpoints[index], starts from 0
retrofit.endpoints[0].identity=ai # The system id
retrofit.endpoints[0].baseUrl: http://127.0.0.1:10010 # The system prefix url
```

You can now `@Autowired` the retrofit interface to send real http request. e.g.:

```java
@Autowired
private final AiAgentEndpoint agentEndpoint;

agentEndpoint.getAgents(1L, 1L);
```

### TODO List

- [X] Support multiply retrofit instance.
- [X] Jackson deserializer should omit the properties's informal case format.
- [X] Improve async http request support.
- [X] Remove the `Retrofit.Builder`, using a single bean to hold the real retrofit factory.
- [X] Retry in limit times if request failed.
- [ ] Drop `@RetrofitServiceScan` annotation or auto-configure it.

---

> Last modified on: 2017-06-14 07:37
