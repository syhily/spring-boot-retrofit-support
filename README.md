## Retrofit Springboot Auto Configuration

This project is mainly for integration with other business system. We use springboot to
get better configuration and dependence inversion.

### Architecture

```
com.oneapm.touch.retrofit
 ├── autoconfigure: Retrofit configuration module and its configuration properties definition
 └── boot: the bean registry factory for building retrofit endpoint interface instance
```

### Retrofit configuration

**YAML**

```yaml
# The http configuration part for integration with other system
retrofit:
  connection:
    timeout: 5000 # The timeout for http request, mile seconds, so 5000 means 5 seconds
    maxIdleConnections: 5 # The maximum number of idle connections for each address.
    keepAliveDuration: 5 # The time (minutes) to live for each idle connections.

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
retrofit.connection.timeout=5000
retrofit.connection.max-idle-connections=5
retrofit.connection.keep-alive-duration=5
# The list of outer system to communicate with should have a index form like 
# retrofit.endpoints[index], starts from 0
retrofit.endpoints[0].identity=ai # The system id
retrofit.endpoints[0].baseUrl: http://127.0.0.1:10010 # The system prefix url

alert.integrate.mi.enable=true # Enable mi module
alert.integrate.mi.duration-in-minutes=30 # Wiki: http://wiki.oneapm.me/pages/viewpage.action?pageId=16650476&focusedCommentId=18481809#comment-18481809
```

### TODO List

- [ ] Drop `@RetrofitServiceScan` annotation or auto-configure it.

---

> Last modified on: 2017-05-31 12:37
