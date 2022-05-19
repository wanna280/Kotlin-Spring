package com.wanna.cloud.client.loadbalancer

import com.wanna.cloud.client.ServiceInstance

/**
 * ServiceInstance的Chooser，去给定具体的策略实现负载均衡
 */
interface ServiceInstanceChooser {

    /**
     * 给定一个serviceId，从该serviceId当中的所有实例当中，使用具体的策略，去选择出一个合适的ServiceInstance去进行返回
     *
     * @param serviceId serviceId
     * @return 从指定的service当中，最终选择出来的合适的ServiceInstance
     */
    fun choose(serviceId: String): ServiceInstance?
}