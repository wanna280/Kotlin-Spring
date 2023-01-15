package com.wanna.cloud.openfeign.ribbon

import com.netflix.loadbalancer.ILoadBalancer
import com.wanna.cloud.netflix.ribbon.SpringClientFactory
import com.wanna.framework.util.StringUtils
import feign.Client
import feign.Request
import feign.Response
import java.net.URI

/**
 * 支持LoadBalancer进行负载均衡的FeignClient, 使用负载均衡策略去完成远程调用并获取请求的最终结果
 *
 * @param springClientFactory Spring ClientFactory 来自于Ribbon, 用来实现负载均衡
 * @param delegate 委托的FeignClient, 完成真正的网络请求的处理(包装ApacheHttpClient/OkHttp等的Client)
 */
open class RibbonLoadBalancerFeignClient(val springClientFactory: SpringClientFactory, val delegate: Client) :
    Client {
    override fun execute(request: Request, options: Request.Options?): Response {
        // request.url获取到请求的URI路径, 将协议/主机名/路径分开
        val uri = URI.create(request.url())
        val serviceId = uri.host

        // 使用Ribbon的LoadBalancer, 从注册中心(ServiceRegistry)当中去获取到一个合适的ServiceInstance实例
        val loadBalancer = springClientFactory.getInstance(serviceId, ILoadBalancer::class.java)!!
        val server = loadBalancer.chooseServer(serviceId)
            ?: throw IllegalStateException("没有从注册中心当中找到合适的ServiceInstance去处理本次请求")

        // 解析成为一个正确的uri, 并构建一个新的request去执行真正的请求的发送
        val path = uri.scheme + "://" + server.hostPort + getCleanPath(uri.path)
        return delegate.execute(createNewRequest(request, path), options)
    }

    /**
     * 构建一个新的request, 因为request不能修改, 但是这里我们需要替换掉path, 因此我们这里需要重新构建一个request;
     * 并将原来的request当中的headers/body/requestMethod等全部拷贝过去
     *
     * @param request 原始的request
     * @param path 需要替换的路径(path)
     * @return 重新构建的Request
     */
    private fun createNewRequest(request: Request, path: String): Request {
        return Request.create(
            request.httpMethod(),
            path,
            request.headers(),
            Request.Body.create(request.body()),
            request.requestTemplate()
        )
    }

    /**
     * 获取一个干净的路径;
     * * 1.如果path不是以"/"作为开头, 那么需要拼接上"/"
     * * 2.如果path以"/"作为结尾, 那么需要去掉"/"
     */
    private fun getCleanPath(path: String): String {
        var result = path
        if (StringUtils.hasText(path)) {
            if (!path.startsWith("/")) {
                result = "/$path"
            } else if (path.endsWith("/")) {
                result = path.substring(0, path.length - 1)
            }
        }
        return result ?: ""
    }
}