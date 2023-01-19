package com.wanna.cloud.client.loadbalancer

import com.wanna.cloud.client.ServiceInstance
import java.net.URI

/**
 * ## LoadBalancerClient
 * * 1.这是一个支持负载均衡的Client, 它本身也是一个ServiceInstanceChooser的子接口, 支持去给定一个serviceId去获取到合适的ServiceInstance; 
 * * 2.它通常会整合一个LoadBalancer去实现最终的负载均衡, choose方法也会基于LoadBalancer去进行实现负载均衡; 
 * * 3.在这里(SpringCloud Common)只是会做一层抽象, 具体的实现细节, 完全交给实现方去进行自行实现; 
 * * 4.实现方可以基于Ribbon在SpringCloud的模块去进行实现, 也可以基于SpringCloudLoadBalancer去进行实现
 *
 * ## LoadBalancer从哪来?
 * * 1.这里使用到的LoadBalancer有可能是来自于Ribbon的ILoadBalancer, 也有可能是来自于SpringCloud自家的LoadBalancer; 
 * * 2.这里LoadBalancer具体的实现, 就看LoadBalancer的实现方采用何种方式去提供实现, 抽象层无法精确地落地到各家的具体实现
 *
 * ## 如何使用?
 * * 在完成了负载均衡的自动配置之后, 使用方想要使用负载均衡的策略, 只需注入LoadBalancerClient并且去进行使用即可.
 *
 * @see ServiceInstanceChooser
 */
interface LoadBalancerClient : ServiceInstanceChooser {

    /**
     * 从LoadBalancer当中去选择一个合适的ServiceInstance去执行目标请求, 并拿到响应的结果
     *
     * @param serviceId serviceId
     * @param request 负载均衡的请求(给定一个具体的ServiceInstance, 去进行发送网络请求, 一个callback回调)
     * @return 执行请求的执行结果
     * @param T 执行的返回结果类型
     */
    fun <T> execute(serviceId: String, request: LoadBalancerRequest<T>): T

    /**
     * 从LoadBalancer当中去选择一个合适的ServiceInstance去执行目标请求, 并拿到响应的结果
     *
     * @param serviceId serviceId
     * @param request 负载均衡的请求(给定一个具体的ServiceInstance, 去进行发送网络请求, 一个callback回调)
     * @param serviceInstance 执行请求的ServiceInstance
     * @return 执行请求的执行结果
     * @param T 执行的返回结果类型
     */
    fun <T> execute(serviceId: String, serviceInstance: ServiceInstance, request: LoadBalancerRequest<T>): T

    /**
     * 重新构造URI; 在微服务的情况下, 通过注册中心去进行服务发现时, 原始的uri很可能是"http://serviceName/path"这样的格式; 
     * 在真正地去发送网络请求时, 需要将serviceName转换为真实的uri, 也就是"host:port/path"的格式, 这个方法的作用就是做这层uri的转换工作; 
     *
     * @param serviceInstance 要去进行转换的ServiceInstance
     * @param uri 候选的要去去进行转换的uri(host部分的内容为serviceName的原始uri)
     * @return 将原始的uri经过转换之后的"host:port/path"这样的uri
     */
    fun reconstructURI(serviceInstance: ServiceInstance, uri: URI): URI
}