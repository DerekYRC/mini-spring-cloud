package com.github.cloud.openfeign.ribbon;

import com.netflix.client.ClientException;
import com.netflix.client.ClientRequest;
import com.netflix.client.IResponse;
import feign.Client;
import feign.Request;
import feign.Response;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 具备负载均衡能力的feign client
 *
 * @author derek(易仁川)
 * @date 2022/4/9
 */
public class LoadBalancerFeignClient implements Client {

    private LoadBalancerClient loadBalancerClient;

    private Client delegate;

    public LoadBalancerFeignClient(LoadBalancerClient loadBalancerClient, Client delegate) {
        this.loadBalancerClient = loadBalancerClient;
        this.delegate = delegate;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        //客户端负载均衡
        URI original = URI.create(request.url());
        String serviceId = original.getHost();
        //选择服务实例
        ServiceInstance serviceInstance = loadBalancerClient.choose(serviceId);
        //重建请求URI
        URI uri = loadBalancerClient.reconstructURI(serviceInstance, original);

        Response response = delegate.execute(new RibbonRequest(request, uri).toRequest(), options);
        return new RibbonResponse(uri, response).getResponse();
    }

    private static class RibbonRequest extends ClientRequest {

        private Request request;

        public RibbonRequest(Request request, URI uri) {
            this.request = request;
            setUri(uri);
        }

        @SuppressWarnings("deprecation")
        private Request toRequest() {
            return Request.create(request.httpMethod(), getUri().toASCIIString(), new HashMap<>(),
                    request.body(), StandardCharsets.UTF_8);
        }
    }

    private static class RibbonResponse implements IResponse {

        private final URI uri;

        private final Response response;

        protected RibbonResponse(URI uri, Response response) {
            this.uri = uri;
            this.response = response;
        }

        @Override
        public Object getPayload() throws ClientException {
            return response.body();
        }

        @Override
        public boolean hasPayload() {
            return response.body() != null;
        }

        @Override
        public boolean isSuccess() {
            return response.status() == 200;
        }

        @Override
        public URI getRequestedURI() {
            return uri;
        }

        @Override
        public Map<String, ?> getHeaders() {
            return new HashMap<>();
        }

        @Override
        public void close() throws IOException {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }

        public Response getResponse() {
            return response;
        }
    }
}
