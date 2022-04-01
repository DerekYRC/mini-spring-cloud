# [前言](#前言)

前置知识。阅读spring、springboot、spring cloud三者的源码必须严格按照 spring => springboot => spring cloud 的顺序进行，阅读spring cloud源码的必备前置知识：

- spring，推荐本人写的简化版的spring框架 [**mini-spring**](https://github.com/DerekYRC/mini-spring/blob/main/README_CN.md) 。熟悉spring源码，阅读springboot源码会非常轻松。
- springboot，重点掌握：1、启动流程 2、**自动装配的原理! 自动装配的原理!! 自动装配的原理!!!** 推荐文章:
  - [《Spring Boot精髓：启动流程源码分析》](https://www.cnblogs.com/java-chen-hao/p/11829344.html)
  - [《细说SpringBoot的自动装配原理》](https://blog.csdn.net/qq_38526573/article/details/107084943)
  - [《Spring Boot 自动装配原理》](https://www.cnblogs.com/javaguide/p/springboot-auto-config.html)

关于spring cloud。spring cloud是构建通用模式的分布式系统的工具集，通过[**spring-cloud-commons**](https://github.com/spring-cloud/spring-cloud-commons) 定义了统一的抽象API，相当于定义了一套协议标准，具体的实现需要符合这套协议标准。spring cloud官方整合第三方组件Eureka、Ribbon、Hystrix等实现了spring-cloud-netflix，阿里巴巴结合自身的Nacos、Sentinel等实现了spring-cloud-alibaba。本项目基于spring-cloud-commons的协议标准自主开发或整合第三方组件提供具体的实现。

写作本项目的目的之一是降低阅读原始spring cloud源码的难度。希望掌握本项目讲解的内容之后再阅读原始spring-cloud的源码能起到事半功倍的效果，所以本项目的功能实现逻辑及原理和官方保持一致但追求代码最大精简化，可以理解为一个源码导读的项目。

技术能力有限且文采不佳，大家可以在此[**issue**](https://github.com/DerekYRC/mini-spring-cloud/issues/1) 留言提问题和发表建议，也欢迎Pull Request完善此项目。

# [服务注册](#服务注册)
> 分支: service-registry
 
为了演示，写一个非常简单的单机版的服务注册和发现中心，命名图图
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
     * 服务注册
     *
     * @param serviceName
     * @param ip
     * @param port
     * @return
     */
    @PostMapping("register")
    public boolean register(@RequestParam("serviceName") String serviceName, @RequestParam("ip") String ip, @RequestParam("port") Integer port) {
        logger.info("register service, serviceName: {}, ip: {}, port: {}", serviceName, ip, port);
        serverMap.putIfAbsent(serviceName.toLowerCase(), Collections.synchronizedSet(new HashSet<>()));
        Server server = new Server(ip, port);
        serverMap.get(serviceName).add(server);
        return true;
    }

    /**
     * 服务注销
     *
     * @param serviceName
     * @param ip
     * @param port
     * @return
     */
    @PostMapping("deregister")
    public boolean deregister(@RequestParam("serviceName") String serviceName, @RequestParam("ip") String ip, @RequestParam("port") Integer port) {
        logger.info("deregister service, serviceName: {}, ip: {}, port: {}", serviceName, ip, port);
        Set<Server> serverSet = serverMap.get(serviceName.toLowerCase());
        if (serverSet != null) {
            Server server = new Server(ip, port);
            serverSet.remove(server);
        }
        return true;
    }

    /**
     * 根据服务名称查询服务列表
     *
     * @param serviceName
     * @return
     */
    @GetMapping("list")
    public Set<Server> list(@RequestParam("serviceName") String serviceName) {
        Set<Server> serverSet = serverMap.get(serviceName.toLowerCase());
        logger.info("list service, serviceName: {}, serverSet: {}", serviceName, JSON.toJSONString(serverSet));
        return serverSet != null ? serverSet : Collections.emptySet();
    }

    /**
     * 查询所有服务名称列表
     *
     * @return
     */
    @GetMapping("listServiceNames")
    public Enumeration<String> listServiceNames() {
        return serverMap.keys();
    }

    /**
     * 服务
     */
    public static class Server {
        private String ip;

        private Integer port;
        
        //Construct、Getters、equals、hashCode
    }
}
```
配置application.yml:
```yaml
server:
  port: 6688
```

spring-cloud-commons服务注册相关API:
![](./assets/service-registry-api.png)
- ServiceInstance和Registration，表示系统中服务的实例
- ServiceRegistry，服务注册和注销接口
- AbstractAutoServiceRegistration，自动注册和注销服务。监听WebServerInitializedEvent(Web服务启动完毕事件)，WebServerInitializedEvent触发时注册服务实例；@PreDestroy注解修饰的方法注销服务实例。

服务注册功能实现:

TutuDiscoveryProperties，配置服务注册中心地址:
```java
@ConfigurationProperties("spring.cloud.tutu.discovery")
public class TutuDiscoveryProperties {

    @Autowired
    private InetUtils inetUtils;

    private String serverAddr;

    private String service;

    private String ip;

    private int port = -1;

    private boolean secure = false;

    @PostConstruct
    public void init() throws Exception {
        if (!StringUtils.hasLength(ip)) {
            //获取服务IP地址
            ip = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
        }
    }

    //getters and setters
}
```
TutuRegistration，图图服务注册实例:
```java
public class TutuRegistration implements Registration {

    private TutuDiscoveryProperties tutuDiscoveryProperties;

    public TutuRegistration(TutuDiscoveryProperties tutuDiscoveryProperties) {
        this.tutuDiscoveryProperties = tutuDiscoveryProperties;
    }

    @Override
    public boolean isSecure() {
        return tutuDiscoveryProperties.isSecure();
    }

    @Override
    public URI getUri() {
        return DefaultServiceInstance.getUri(this);
    }
  
    //getters and setters
}
```
注册和注销TutuRegistration的接口TutuServiceRegistry:
```java
public class TutuServiceRegistry implements ServiceRegistry<Registration> {
  private static final Logger logger = LoggerFactory.getLogger(TutuServiceRegistry.class);

  private TutuDiscoveryProperties tutuDiscoveryProperties;

  public TutuServiceRegistry(TutuDiscoveryProperties tutuDiscoveryProperties) {
    this.tutuDiscoveryProperties = tutuDiscoveryProperties;
  }

  /**
   * 注册服务实例
   *
   * @param registration
   */
  @Override
  public void register(Registration registration) {
    Map<String, Object> param = new HashMap<>();
    param.put("serviceName", tutuDiscoveryProperties.getService());
    param.put("ip", tutuDiscoveryProperties.getIp());
    param.put("port", tutuDiscoveryProperties.getPort());

    String result = HttpUtil.post(tutuDiscoveryProperties.getServerAddr() + "/register", param);
    if (Boolean.parseBoolean(result)) {
      logger.info("register service successfully, serviceName: {}, ip: {}, port: {}",
              tutuDiscoveryProperties.getService(), tutuDiscoveryProperties.getIp(), tutuDiscoveryProperties.getPort());
    } else {
      logger.error("register service failed, serviceName: {}, ip: {}, port: {}",
              tutuDiscoveryProperties.getService(), tutuDiscoveryProperties.getIp(), tutuDiscoveryProperties.getPort());
      throw new RuntimeException("register service failed, serviceName");
    }
  }

  /**
   * 注销服务实例
   *
   * @param registration
   */
  @Override
  public void deregister(Registration registration) {
    Map<String, Object> param = new HashMap<>();
    param.put("serviceName", tutuDiscoveryProperties.getService());
    param.put("ip", tutuDiscoveryProperties.getIp());
    param.put("port", tutuDiscoveryProperties.getPort());

    String result = HttpUtil.post(tutuDiscoveryProperties.getServerAddr() + "/deregister", param);
    if (Boolean.parseBoolean(result)) {
      logger.info("de-register service successfully, serviceName: {}, ip: {}, port: {}",
              tutuDiscoveryProperties.getService(), tutuDiscoveryProperties.getIp(), tutuDiscoveryProperties.getPort());
    } else {
      logger.warn("de-register service failed, serviceName: {}, ip: {}, port: {}",
              tutuDiscoveryProperties.getService(), tutuDiscoveryProperties.getIp(), tutuDiscoveryProperties.getPort());
    }
  }
}
```
AbstractAutoServiceRegistration实现类:
```java
public class TutuAutoServiceRegistration extends AbstractAutoServiceRegistration<Registration> {

    private TutuRegistration tutuRegistration;

    protected TutuAutoServiceRegistration(ServiceRegistry<Registration> serviceRegistry, TutuRegistration tutuRegistration) {
        super(serviceRegistry, null);
        this.tutuRegistration = tutuRegistration;
    }

    @Override
    protected Registration getRegistration() {
        if (tutuRegistration.getPort() < 0) {
            //设置服务端口
            tutuRegistration.setPort(this.getPort().get());
        }
        return tutuRegistration;
    }
}
```
自动装配:
TutuServiceRegistryAutoConfiguration:
```java
/**
 * 自动配置服务注册相关类
 */
@Configuration
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
public class TutuServiceRegistryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TutuDiscoveryProperties tutuProperties() {
        return new TutuDiscoveryProperties();
    }

    @Bean
    public TutuRegistration tutuRegistration(TutuDiscoveryProperties tutuDiscoveryProperties) {
        return new TutuRegistration(tutuDiscoveryProperties);
    }

    @Bean
    public TutuServiceRegistry tutuServiceRegistry(TutuDiscoveryProperties tutuDiscoveryProperties) {
        return new TutuServiceRegistry(tutuDiscoveryProperties);
    }

    @Bean
    public TutuAutoServiceRegistration tutuAutoServiceRegistration(ServiceRegistry<Registration> serviceRegistry, TutuRegistration tutuRegistration) {
        return new TutuAutoServiceRegistration(serviceRegistry, tutuRegistration);
    }
}
```
META-INF/spring.factories:
```yaml
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.github.cloud.tutu.registry.TutuServiceRegistryAutoConfiguration
```

测试:

1、maven install

![](./assets/service-registry-maven.png)

2、启动服务注册和发现中心TutuServerApplication

3、启动服务提供者ProviderApplication，其代码如下:
```java
@RestController
@SpringBootApplication
public class ProviderApplication {

    @Value("${server.port}")
    private Integer port;

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

    @PostMapping("/echo")
    public String echo() {
        return "Port of the service provider: " + port;
    }
}
```
配置application.yml:
```yaml
spring:
  application:
    name: provider-application
  cloud:
    tutu:
      discovery:
        server-addr: localhost:6688
        service: ${spring.application.name}

# 随机端口
server:
  port: ${random.int[10000,20000]}
```

4、浏览器中访问http://localhost:6688/list?serviceName=provider-application 或执行命令 ```curl -X GET 'http://localhost:6688/list?serviceName=provider-application'``` ，响应报文如下，说明服务已经注册到服务注册中心
```json
[
  {
  "ip": "192.168.47.1",
  "port": 19588
  }
]
```

# [服务发现](#服务发现)
> 分支: service-discovery

spring-cloud-commons定义的服务发现接口```org.springframework.cloud.client.discovery.DiscoveryClient```:
```java
public interface DiscoveryClient extends Ordered {

	/**
	 * Gets all ServiceInstances associated with a particular serviceId.
	 * @param serviceId The serviceId to query.
	 * @return A List of ServiceInstance.
	 */
	List<ServiceInstance> getInstances(String serviceId);

	/**
	 * @return All known service IDs.
	 */
	List<String> getServices();
}
```

仅需实现DiscoveryClient接口即可，实现类:
```java
/**
 * 服务发现实现类
 */
public class TutuDiscoveryClient implements DiscoveryClient {
    private static final Logger logger = LoggerFactory.getLogger(TutuDiscoveryClient.class);

    private TutuDiscoveryProperties tutuDiscoveryProperties;

    public TutuDiscoveryClient(TutuDiscoveryProperties tutuDiscoveryProperties) {
        this.tutuDiscoveryProperties = tutuDiscoveryProperties;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        Map<String, Object> param = new HashMap<>();
        param.put("serviceName", serviceId);

        String response = HttpUtil.get(tutuDiscoveryProperties.getServerAddr() + "/list", param);
        logger.info("query service instance, serviceId: {}, response: {}", serviceId, response);
        return JSON.parseArray(response).stream().map(hostInfo -> {
            TutuServiceInstance serviceInstance = new TutuServiceInstance();
            serviceInstance.setServiceId(serviceId);
            String ip = ((JSONObject) hostInfo).getString("ip");
            Integer port = ((JSONObject) hostInfo).getInteger("port");
            serviceInstance.setHost(ip);
            serviceInstance.setPort(port);
            return serviceInstance;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> getServices() {
        String response = HttpUtil.post(tutuDiscoveryProperties.getServerAddr() + "/listServiceNames", new HashMap<>());
        logger.info("query service instance list, response: {}", response);
        return JSON.parseArray(response, String.class);
    }
}
```

自动装配TutuDiscoveryAutoConfiguration:
```java
@Configuration
public class TutuDiscoveryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TutuDiscoveryProperties tutuDiscoveryProperties() {
        return new TutuDiscoveryProperties();
    }

    @Bean
    public DiscoveryClient tutuDiscoveryClient(TutuDiscoveryProperties tutuDiscoveryProperties) {
        return new TutuDiscoveryClient(tutuDiscoveryProperties);
    }
}
```
spring.factories:
```yaml
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.github.cloud.tutu.registry.TutuServiceRegistryAutoConfiguration,\
  com.github.cloud.tutu.discovery.TutuDiscoveryAutoConfiguration
```

测试:

1、maven install，启动服务注册和发现中心TutuServerApplication，启动服务提供者ProviderApplication，启动服务消费者ConsumerApplication(后续测试步骤均同此，不再提及)

服务消费者代码如下:
```java
@SpringBootApplication
public class ConsumerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ConsumerApplication.class, args);
  }

  @RestController
  static class HelloController {

    @Autowired
    private DiscoveryClient discoveryClient;

    private RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/hello")
    public String hello() {
      List<ServiceInstance> serviceInstances = discoveryClient.getInstances("provider-application");
      if (serviceInstances.size() > 0) {
        ServiceInstance serviceInstance = serviceInstances.get(0);
        URI uri = serviceInstance.getUri();
        String response = restTemplate.postForObject(uri.toString() + "/echo", null, String.class);
        return response;
      }

      throw new RuntimeException("No service instance for provider-application found");
    }
  }
}
```
application.yml:
```yaml
spring:
  application:
    name: consumer-application
  cloud:
    tutu:
      discovery:
        server-addr: localhost:6688
        service: ${spring.application.name}
```

2、访问http://localhost:8080/hello ,相应报文如下:
```yaml
Port of the service provider: 19922
```


































