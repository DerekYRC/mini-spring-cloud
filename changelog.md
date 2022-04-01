# [前言](#前言)

前置知识。阅读spring、springboot、spring cloud三者的源码必须严格按照 spring => springboot => spring cloud 的顺序进行，阅读spring cloud源码的必备前置知识：

- spring，推荐本人写的简化版的spring框架 [**mini-spring**](https://github.com/DerekYRC/mini-spring/blob/main/README_CN.md) 。熟悉spring源码，阅读springboot源码会非常轻松。
- springboot，重点掌握：1、启动流程 2、**自动装配的原理! 自动装配的原理!! 自动装配的原理!!!** 推荐文章:
  - [《Spring Boot精髓：启动流程源码分析》](https://www.cnblogs.com/java-chen-hao/p/11829344.html)
  - [《细说SpringBoot的自动装配原理》](https://blog.csdn.net/qq_38526573/article/details/107084943)
  - [《Spring Boot 自动装配原理》](https://www.cnblogs.com/javaguide/p/springboot-auto-config.html)

关于spring cloud。spring cloud是构建通用模式的分布式系统的工具集，通过[**spring-cloud-commons**](https://github.com/spring-cloud/spring-cloud-commons) 定义了统一的抽象API，相当于定义了一套协议标准，具体的实现需要符合这套协议标准。spring cloud官方整合第三方组件Eureka、Ribbon、Hystrix等实现了spring-cloud-netflix，阿里巴巴结合自身的Nacos、Sentinel等实现了spring-cloud-alibaba。本项目基于spring-cloud-commons的协议标准自主开发或整合第三方组件提供具体的实现。

写作本项目的目的之一是降低阅读原始spring cloud源码的难度。希望掌握本项目讲解的内容之后再阅读原始spring-cloud的源码能起到事半功倍的效果，所以本项目的功能实现逻辑及原理和官方保持一致但追求代码最大精简化，可以理解为一个源码导读的项目。

技术能力有限且文采欠佳，大家可以在此[**issue**](https://github.com/DerekYRC/mini-spring-cloud/issues/1) 留言提问题和发表建议，也欢迎Pull Request完善此项目。

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


# [集成ribbon实现客户端负载均衡](#集成ribbon实现客户端负载均衡)
> 分支: load-balancer

## 关于ribbon
> (翻译自官方文档)ribbon是一个提供如下功能的依赖包:
> - 负载均衡
> - 容错机制
> - 支持多种协议(HTTP, TCP, UDP)，支持异步和响应式的调用方式
> - 缓存和批处理

#### ribbon核心API

一、IClientConfig接口

![](./assets/load-balancer-IClientConfig.png)

定义加载和读取ribbon客户端配置的方法，实现类DefaultClientConfigImpl

二、IPing接口

![](./assets/load-balancer-IPing.png)

顾名思义，判断服务是否存活，实现类:

- NoOpPing，不做检查，认为服务存活
- DummyPing，不做检查，认为服务存活

三、ServerList接口

![](./assets/load-balancer-ServerList.png)

获取服务实例列表的接口，实现类:

- ConfigurationBasedServerList，基于配置获取服务实例列表

四、IRule接口

![](./assets/load-balancer-IRule.png)

负载均衡规则，实现类:

- RoundRobinRule，轮询
- RandomRule，随机
- WeightedResponseTimeRule，根据响应时间分配权重，响应时间越短权重越大
- BestAvailableRule，跳过被熔断器标记为"tripped"状态的、并且选择并发请求数最小的服务实例
- ZoneAvoidanceRule，根据所属zone和可用性筛选服务实例，在没有多zone的情况下退化为轮询RoundRobinRule
- AvailabilityFilteringRule，过滤掉一直连接失败或活跃连接数超过配置值的服务实例
- RetryRule，对其他负载均衡规则的包装，在一段时间内失败重试

五、ServerListFilter接口

![](./assets/load-balancer-ServerListFilter.png)

服务实例过滤器

六、ServerListUpdater接口

![](./assets/load-balancer-ServerListUpdater.png)

PollingServerListUpdater，起一个周期任务更新服务实例列表

七、ILoadBalancer接口

![](./assets/load-balancer-ILoadBalancer.png)

负载均衡接口，实现类：

- BaseLoadBalancer，手动设置服务实例，根据负载均衡规则IRule筛选服务实例
- DynamicServerListLoadBalancer，使用ServerListUpdater动态更新服务实例列表
- ZoneAwareLoadBalancer，支持zone

#### 集成ribbon实现客户端负载均衡(一)

spring-cloud-commons负载均衡相关API:

- ServiceInstanceChooser接口，服务实例选择器，根据服务提供者的服务名称选择服务实例

```java
/**
 * Implemented by classes which use a load balancer to choose a server to send a request to.
 */
public interface ServiceInstanceChooser {

	/**
	 * Chooses a ServiceInstance from the LoadBalancer for the specified service.
	 */
	ServiceInstance choose(String serviceId);

	/**
	 * Chooses a ServiceInstance from the LoadBalancer for the specified service and LoadBalancer request.
	 */
	<T> ServiceInstance choose(String serviceId, Request<T> request);
}
```

- LoadBalancerClient接口

```java
/**
 * Represents a client-side load balancer.
 */
public interface LoadBalancerClient extends ServiceInstanceChooser {

	/**
	 * Executes request using a ServiceInstance from the LoadBalancer for the specified service.
	 */
	<T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException;

	/**
	 * Executes request using a ServiceInstance from the LoadBalancer for the specified service.
	 */
	<T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) throws IOException;

	/**
	 * Creates a proper URI with a real host and port for systems to utilize. Some systems
	 * use a URI with the logical service name as the host, such as
	 * http://myservice/path/to/service. This will replace the service name with the
	 * host:port from the ServiceInstance.
	 */
	URI reconstructURI(ServiceInstance instance, URI original);
}
```

本节只关注ServiceInstanceChooser接口的choose方法，下一节讲解LoadBalancerClient接口的三个方法。

负载均衡功能实现:

RibbonClientConfiguration，配置ribbon核心API默认实现类:

```java
/**
 * 配置ribbon默认组件
 */
@Configuration
public class RibbonClientConfiguration {

    @Value("${ribbon.client.name}")
    private String name;

    @Bean
    @ConditionalOnMissingBean
    public IClientConfig ribbonClientConfig() {
        DefaultClientConfigImpl config = new DefaultClientConfigImpl();
        config.loadProperties(name);
        return config;
    }

    @Bean
    @ConditionalOnMissingBean
    public IRule ribbonRule(IClientConfig config) {
        ZoneAvoidanceRule rule = new ZoneAvoidanceRule();
        rule.initWithNiwsConfig(config);
        return rule;
    }

    @Bean
    @ConditionalOnMissingBean
    public IPing ribbonPing(IClientConfig config) {
        return new DummyPing();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerList<Server> ribbonServerList(IClientConfig config) {
        ConfigurationBasedServerList serverList = new ConfigurationBasedServerList();
        serverList.initWithNiwsConfig(config);
        return serverList;
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerListUpdater ribbonServerListUpdater(IClientConfig config) {
        return new PollingServerListUpdater(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerListFilter<Server> ribbonServerListFilter(IClientConfig config) {
        ServerListSubsetFilter filter = new ServerListSubsetFilter();
        filter.initWithNiwsConfig(config);
        return filter;
    }

    @Bean
    @ConditionalOnMissingBean
    public ILoadBalancer ribbonLoadBalancer(IClientConfig config,
                                            ServerList<Server> serverList, ServerListFilter<Server> serverListFilter,
                                            IRule rule, IPing ping, ServerListUpdater serverListUpdater) {
        return new ZoneAwareLoadBalancer<>(config, rule, ping, serverList,
                serverListFilter, serverListUpdater);
    }
}
```

只需实现ribbon核心API中的获取服务实例列表接口ServerList，实现类TutuServerList:

```java
/**
 * 查询图图服务实例列表
 */
public class TutuServerList extends AbstractServerList<TutuServer> {
    private static Logger logger = LoggerFactory.getLogger(TutuServerList.class);

    private TutuDiscoveryProperties discoveryProperties;

    private String serviceId;

    public TutuServerList(TutuDiscoveryProperties discoveryProperties) {
        this.discoveryProperties = discoveryProperties;
    }

    /**
     * 查询服务实例列表
     *
     * @return
     */
    @Override
    public List<TutuServer> getInitialListOfServers() {
        return getServer();
    }

    /**
     * 查询服务实例列表
     *
     * @return
     */
    @Override
    public List<TutuServer> getUpdatedListOfServers() {
        return getServer();
    }

    private List<TutuServer> getServer() {
        Map<String, Object> param = new HashMap<>();
        param.put("serviceName", serviceId);

        String response = HttpUtil.get(discoveryProperties.getServerAddr() + "/list", param);
        logger.info("query service instance, serviceId: {}, response: {}", serviceId, response);
        return JSON.parseArray(response).stream().map(hostInfo -> {
            String ip = ((JSONObject) hostInfo).getString("ip");
            Integer port = ((JSONObject) hostInfo).getInteger("port");
            return new TutuServer(ip, port);
        }).collect(Collectors.toList());
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
        this.serviceId = iClientConfig.getClientName();
    }
}
```

配置TutuServerList，替换RibbonClientConfiguration中配置的默认实现:

```java
@Configuration
@RibbonClients(defaultConfiguration = TutuRibbonClientConfiguration.class)
public class RibbonTutuAutoConfiguration {
}
```

```java
/**
 * 自定义ribbon组件
 */
@Configuration
public class TutuRibbonClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList(IClientConfig config,
                                          TutuDiscoveryProperties discoveryProperties) {
        TutuServerList serverList = new TutuServerList(discoveryProperties);
        serverList.initWithNiwsConfig(config);
        return serverList;
    }
}
```

每一个Provider服务对应一套ribbon核心API，相互隔离，SpringClientFactory为每一个Provider服务对应的ribbon核心API创建一个子spring应用上下文（ApplicationContext）。

子spring应用上下文的配置类来自于:

- SpringClientFactory的构造函数参数RibbonClientConfiguration配置类
- 修饰RibbonTutuAutoConfiguration的注解指定的属性defaultConfiguration = TutuRibbonClientConfiguration配置类（处理RibbonClients注解的RibbonClientConfigurationRegistrar，会将TutuRibbonClientConfiguration配置类包装为RibbonClientSpecification供SpringClientFactory使用）

为了充分理解子spring容器的创建逻辑，可以在下面的测试环节debug如下几个方法:

- RibbonClientConfigurationRegistrar#registerBeanDefinitions
- RibbonAutoConfiguration#springClientFactory
- SpringClientFactory的构造函数和方法

LoadBalancerClient实现类RibbonLoadBalancerClient:

```java
/**
 * ribbon负载均衡客户端
 */
public class RibbonLoadBalancerClient implements LoadBalancerClient {

    private SpringClientFactory clientFactory;

    public RibbonLoadBalancerClient(SpringClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    /**
     * 选择服务实例
     */
    @Override
    public ServiceInstance choose(String serviceId) {
        return choose(serviceId, null);
    }

    /**
     * 选择服务实例
     */
    @Override
    public <T> ServiceInstance choose(String serviceId, Request<T> request) {
        ILoadBalancer loadBalancer = clientFactory.getInstance(serviceId, ILoadBalancer.class);
        Server server = loadBalancer.chooseServer("default");
        if (server != null) {
            return new TutuServiceInstance(serviceId, server.getHost(), server.getPort());
        }

        return null;
    }
}
```

自动装配spring.factories

```yaml
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.github.cloud.loadbalancer.ribbon.config.RibbonAutoConfiguration,\
  com.github.cloud.loadbalancer.ribbon.config.RibbonTutuAutoConfiguration
```

测试:

1、在mini-spring-cloud-provider-example文件夹下执行命令```mvn spring-boot:run```启动多个服务提供者

服务消费者代码:

```java
@SpringBootApplication
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
    
    @RestController
    static class HelloController {

      private RestTemplate restTemplate = new RestTemplate();

        @GetMapping("/world")
        public String world() {
            ServiceInstance serviceInstance = loadBalancerClient.choose("provider-application");
            if (serviceInstance != null) {
                URI uri = serviceInstance.getUri();
                String response = restTemplate.postForObject(uri.toString() + "/echo", null, String.class);
                return response;
            }

            throw new RuntimeException("No service instance for provider-application found");
        }
    }
}
```

2、多次访问```http://localhost:8080/world```, 通过响应报文中的端口可知请求以轮询的方式分配给服务提供者(默认的负载均衡规则ZoneAvoidanceRule在没有多zone的情况下退化为轮询规则)

#### 集成ribbon实现客户端负载均衡(二)

简化调用方式，达到如下的效果，使用服务提供者的名称替换IP和端口

```java
restTemplate.postForObject("http://provider-application/echo", null, String.class);
```

实现LoadBalancerClient的execute方法和reconstructURI方法:

```java
public class RibbonLoadBalancerClient implements LoadBalancerClient {

    /**
     * 重建请求URI，将服务名称替换为服务实例的IP:端口
     */
    @Override
    public URI reconstructURI(ServiceInstance server, URI original) {
        try {
            //将服务名称替换为服务实例的IP:端口，例如http://provider-application/echo被重建为http://192.168.100.1:8888/echo
            StringBuilder sb = new StringBuilder();
            sb.append(original.getScheme()).append("://");
            sb.append(server.getHost());
            sb.append(":").append(server.getPort());
            sb.append(original.getRawPath());
            if (StrUtil.isNotEmpty(original.getRawQuery())) {
                sb.append("?").append(original.getRawQuery());
            }
            URI newURI = new URI(sb.toString());
            return newURI;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理http请求
     */
    @Override
    public <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException {
        ServiceInstance serviceInstance = choose(serviceId);
        return execute(serviceId, serviceInstance, request);
    }

    /**
     * 处理http请求
     *
     */
    @Override
    public <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) throws IOException {
        try {
            return request.apply(serviceInstance);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
```

- reconstructURI方法，重建请求URI，将服务名称替换为服务实例的IP:端口，例如http://provider-application/echo被重建为http://192.168.100.1:8888/echo
- execute方法，处理http请求

有了RibbonLoadBalancerClient的reconstructURI和execute方法，将所有http请求委托给RibbonLoadBalancerClient即可。其实spring-cloud-commons已经帮我们配置好拦截RestTemplate的http请求委托给RibbonLoadBalancerClient的拦截器LoadBalancerInterceptor，配置类如下:

![](./assets/load-balancer-LoadBalancerAutoConfiguration.png)

LoadBalancerAutoConfiguration配置类为每一个被LoadBalanced注解修饰的RestTemplate增加LoadBalancerInterceptor拦截器。

![](./assets/load-balancer-LoadBalancerInterceptor.png)

LoadBalancerInterceptor将http请求委托给LoadBalancerClient执行，其中requestFactory.createRequest使用ServiceRequestWrapper包装原始的http请求

![](./assets/load-balancer-ServiceRequestWrapper.png)

ServiceRequestWrapper调用LoadBalancerClient#reconstructURI方法重建请求URI，将服务名称替换为服务实例的IP:端口

测试:

服务消费者代码如下:

```java
@SpringBootApplication
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @Configuration
    static class RestTemplateConfiguration {

        /**
         * 赋予负载均衡的能力
         *
         * @return
         */
        @LoadBalanced
        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }

    @RestController
    static class HelloController {

        @Autowired
        private RestTemplate loadBalancedRestTemplate;

        @GetMapping("/foo")
        public String foo() {
            return loadBalancedRestTemplate.postForObject("http://provider-application/echo", null, String.class);
        }
    }
}
```

访问```http://localhost:8080/foo```

























































