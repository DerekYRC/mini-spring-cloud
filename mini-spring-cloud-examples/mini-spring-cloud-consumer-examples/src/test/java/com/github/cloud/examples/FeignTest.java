package com.github.cloud.examples;

import feign.Feign;
import feign.RequestLine;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.web.bind.annotation.GetMapping;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author derek(易仁川)
 * @date 2022/3/27
 */
public class FeignTest {
    private static Logger logger = LoggerFactory.getLogger(FeignTest.class);

    interface HelloService {

        @RequestLine("GET /hello")
        String hello();
    }

    @Test
    public void testOpenFeign() {
        HelloService helloService = Feign.builder()
                .target(HelloService.class, "http://localhost:8080");
        String response = helloService.hello();
        logger.info("response: {}", response);
        boolean succ = response.startsWith("Port of the service provider");
        assertThat(succ).isTrue();
    }

    interface WorldService {

        @GetMapping("/world")
        String world();
    }

    @Test
    public void testSpringCloudOpenFeign() {
        WorldService worldService = Feign.builder()
                .contract(new SpringMvcContract())
                .target(WorldService.class, "http://localhost:8080");
        String response = worldService.world();
        logger.info("response: {}", response);
        boolean succ = response.startsWith("Port of the service provider");
        assertThat(succ).isTrue();
    }
}
