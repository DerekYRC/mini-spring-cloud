package com.github.cloud.examples;

import com.github.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author derek(易仁川)
 * @date 2022/4/9
 */
@FeignClient("provider-application")
public interface EchoService {

    @PostMapping("echo")
    String echo();
}
