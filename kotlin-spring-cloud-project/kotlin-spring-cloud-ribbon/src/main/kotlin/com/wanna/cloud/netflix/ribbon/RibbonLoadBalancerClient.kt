package com.wanna.cloud.netflix.ribbon

import com.netflix.loadbalancer.Server
import com.wanna.cloud.client.ServiceInstance
import com.wanna.cloud.client.loadbalancer.LoadBalancerClient
import com.wanna.cloud.client.loadbalancer.LoadBalancerRequest
import java.net.URI

/**
 * * Ribbon针对于SpringCloud的实现的LoadBalancerClient, 它组合了SpringClientFactory, 支持从childContext当中, 去获取组件;
 * * 它可以将SpringCloud负载均衡的抽象直接暴露给使用方去进行使用, 对于更多细节, 都在SpringClientFactory当中的对应的childContext当中;
 *
 * ## Note:
 *
 * * 1.在使用方, 想要去使用LoadBalancedClient, 只需要注入LoadBalancerClient, 并给定request的apply方式(callback), 即可完成请求的发送和处理;
 * * 2.既然这个类当中, 是使用到SpringClientFactory去完成的负载均衡, 那么别的地方, 当然可以不注入LoadBalancerClient, 当然也可以注入
 * SpringClientFactory去自己完成处理; 毕竟也是SpringClientFactory在默认情况下是由Ribbon提供的, 也确实是整合的了负载均衡的相关功能(比如ILoadBalancer)
 *
 * @param springClientFactory SpringClientFactory(NamedContextFactory), Spring ClientFactory-->Spring当中的Client的Factory
 * @see LoadBalancerClient
 */
open class RibbonLoadBalancerClient(private val springClientFactory: SpringClientFactory) : LoadBalancerClient {

    override fun <T> execute(serviceId: String, request: LoadBalancerRequest<T>): T {
        val serviceInstance =
            choose(serviceId) ?: throw IllegalStateException("无法找到合适的ServiceInstance去处理请求")
        return execute(serviceId, serviceInstance, request)
    }

    override fun <T> execute(serviceId: String, serviceInstance: ServiceInstance, request: LoadBalancerRequest<T>): T {
        return request.apply(serviceInstance)
    }

    override fun reconstructURI(serviceInstance: ServiceInstance, uri: URI): URI {
        val serviceUri = serviceInstance.getUri()

        // 从ServiceInstance当中去获取到真实的主机地址
        val host: String =
            if (serviceUri.startsWith("http://")) serviceUri.substring(7)
            else if (serviceUri.startsWith("https://")) serviceUri.substring(8)
            else serviceUri

        // 将原始的包含了serviceName的host替换成为ServiceInstance当中的真实host
        val originUri = uri.toString()
        val newUri = originUri.replace(serviceInstance.getServiceId(), host)

        // 构建一个新的URI去进行return
        return URI(newUri)
    }

    /**
     * 选择一个合适的ServiceInstance去处理请求
     *
     * @param serviceId serviceName 在创建childContext时, 会把这个serviceId直接传递给ServerList
     */
    override fun choose(serviceId: String): ServiceInstance? {
        val loadBalancer = springClientFactory.getLoadBalancer(serviceId)!!
        val server = loadBalancer.chooseServer(serviceId) ?: return null
        return RibbonServer(serviceId, server)
    }

    /**
     * 这是将RibbonServer适配到SpringCloud的ServiceInstance的RibbonServer, 起一层桥接的作用
     *
     * @see Server
     * @see ServiceInstance
     */
    class RibbonServer(private val serviceId: String, private val server: Server) : ServiceInstance {
        override fun getServiceId() = serviceId
        override fun getInstanceId(): String = server.id
        override fun getHost(): String = server.host
        override fun getPort(): Int = server.port

        override fun isSecure(): Boolean {
            return getSchema() == "https"
        }

        override fun getSchema(): String = server.scheme

        override fun getUri(): String = "${getSchema()}://${server.hostPort}"

        override fun getMetadata(): Map<String, String> = emptyMap()
    }
}