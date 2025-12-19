# [Preface](#preface)

One of the purposes of writing this project is to reduce the difficulty of reading the original Spring Cloud source code. After mastering the content explained in this project, reading the original Spring Cloud source code should be much more effective. Therefore, this project's functional implementation logic and principles are consistent with the official version but pursue maximum code simplification. **This project can be understood as a source code guide project**.

Prerequisites. Reading the source code of Spring, Spring Boot, and Spring Cloud must strictly follow the order: Spring -> Spring Boot -> Spring Cloud. Essential prerequisites for reading Spring Cloud source code:

- Spring: Recommended is the simplified Spring framework [**mini-spring**](https://github.com/DerekYRC/mini-spring/blob/main/README.md) written by myself. Being familiar with Spring source code makes reading Spring Boot source code very easy.
- Spring Boot: Focus on: 1. Startup process 2. **Auto-configuration principles! Auto-configuration principles!! Auto-configuration principles!!!** Recommended articles:
  - [《Spring Boot Essence: Startup Process Source Code Analysis》](https://www.cnblogs.com/java-chen-hao/p/11829344.html)
  - [《Spring Boot Auto-Configuration Principles, This Article is Enough!》](https://mp.weixin.qq.com/s/f6oED1hbiWat_0HOwxgfnA)
- Spring Cloud: Learn to use it first, then study the source code. Don't put the cart before the horse. Recommended [《Comprehensive Spring Cloud Learning Guide》](http://svip.iocoder.cn/Spring-Cloud/tutorials/).

About Spring Cloud. Spring Cloud is a toolkit for building common patterns in distributed systems. Through [**spring-cloud-commons**](https://github.com/spring-cloud/spring-cloud-commons), it defines unified abstract APIs, equivalent to defining a protocol standard. Specific implementations must conform to this protocol standard. Spring Cloud officially integrates third-party components like Eureka, Ribbon, and Hystrix to develop Spring Cloud Netflix. Alibaba combined its own Nacos, Sentinel, and other components to develop Spring Cloud Alibaba. This project develops independently or integrates third-party components based on the spring-cloud-commons protocol standard to provide specific implementations.

Due to limited technical ability and poor writing skills, everyone can leave comments, ask questions, and make suggestions in this [**issue**](https://github.com/DerekYRC/mini-spring-cloud/issues/1). Pull Requests to improve this project are also welcome.

# [Service Registration](#service-registration)
> Code branch: service-registry

For demonstration purposes, a very simple single-machine service registration and discovery center named Tutu is implemented:

```java
@RestController
@SpringBootApplication
public class TutuServerApplication {
    private static Logger logger = LoggerFactory.getLogger(TutuServerApplication.class);

    private ConcurrentHashMap<String, Set<Server>> serverMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        SpringApplication.run(TutuServerApplication.class, args);
    }

    /**
     * Service registration
     */
    @PostMapping("register")
    public boolean register(@RequestParam("serviceName") String serviceName, 
                          @RequestParam("ip") String ip, 
                          @RequestParam("port") Integer port) {
        logger.info("register service, serviceName: {}, ip: {}, port: {}", serviceName, ip, port);
        serverMap.putIfAbsent(serviceName.toLowerCase(), Collections.synchronizedSet(new HashSet<>()));
        Server server = new Server(ip, port);
        serverMap.get(serviceName).add(server);
        return true;
    }

    /**
     * Service deregistration
     */
    @PostMapping("deregister")
    public boolean deregister(@RequestParam("serviceName") String serviceName, 
                            @RequestParam("ip") String ip, 
                            @RequestParam("port") Integer port) {
        logger.info("deregister service, serviceName: {}, ip: {}, port: {}", serviceName, ip, port);
        Set<Server> serverSet = serverMap.get(serviceName.toLowerCase());
        if (serverSet != null) {
            Server server = new Server(ip, port);
            serverSet.remove(server);
        }
        return true;
    }

    /**
     * Query service list by service name
     */
    @GetMapping("list")
    public Set<Server> list(@RequestParam("serviceName") String serviceName) {
        Set<Server> serverSet = serverMap.get(serviceName.toLowerCase());
        logger.info("list service, serviceName: {}, serverSet: {}", serviceName, JSON.toJSONString(serverSet));
        return serverSet != null ? serverSet : Collections.emptySet();
    }
}
```

# [Service Discovery](#service-discovery)
> Code branch: service-discovery

Implement service discovery capabilities by creating a DiscoveryClient that can retrieve service instances from the Tutu registry.

Key components:
- ServiceInstance: Represents a service instance with IP and port
- DiscoveryClient: Interface for service discovery operations
- TutuDiscoveryClient: Implementation that communicates with Tutu registry

# [Client-Side Load Balancing with Ribbon Integration](#client-side-load-balancing)
> Code branch: loadbalancer-ribbon

Integrate Ribbon for client-side load balancing:
- LoadBalancerClient: Interface for load balancing operations
- RibbonLoadBalancerClient: Implementation using Ribbon algorithms
- Support for multiple load balancing strategies (Round Robin, Random, etc.)

# [Simplifying Calls with Feign Integration](#feign-integration)
> Code branch: openfeign-integration

Integrate OpenFeign to simplify service-to-service calls:
- Declarative HTTP client using annotations
- Automatic service discovery integration
- Request/response serialization
- Error handling and fallback mechanisms

# [API Gateway](#api-gateway)
> Code branch: api-gateway

Implement a simple API Gateway using Zuul:
- Route configuration and management
- Request routing based on service names
- Load balancing for backend services
- Request/response filtering capabilities

# [Traffic Control and Circuit Breaker](#traffic-control-and-circuit-breaker)
> Code branch: circuit-breaker

Implement traffic control and circuit breaker patterns:
- Request rate limiting
- Circuit breaker implementation
- Fallback mechanisms
- Health monitoring and automatic recovery

---

## Key Features Implemented

### Service Registration and Discovery
- Lightweight registry server (Tutu)
- Service registration/deregistration APIs
- Service instance discovery
- Health check mechanisms

### Load Balancing
- Client-side load balancing
- Multiple balancing algorithms
- Integration with service discovery
- Failover support

### Service Communication
- Declarative HTTP clients (Feign)
- Automatic marshalling/unmarshalling
- Service-to-service authentication
- Request tracing and logging

### Resilience Patterns
- Circuit breaker pattern
- Bulkhead pattern
- Timeout handling
- Retry mechanisms

### API Management
- Centralized routing
- Request/response transformation
- Rate limiting and throttling
- Security filters

---

*This changelog documents the evolution of mini-spring-cloud from basic service registration to a comprehensive microservices framework, demonstrating core Spring Cloud concepts through practical implementation.*