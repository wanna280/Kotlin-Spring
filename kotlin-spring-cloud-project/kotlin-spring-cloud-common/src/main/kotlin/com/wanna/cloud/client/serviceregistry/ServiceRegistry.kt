package com.wanna.cloud.client.serviceregistry

/**
 * ServiceInstance的注册中心
 *
 * @see com.wanna.cloud.client.ServiceInstance
 * @param R ServiceInstance类型
 */
interface ServiceRegistry<R : Registration> {

    /**
     * 注册一个实例到ServiceRegistry当中
     *
     * @param registration 要进行注册的ServiceInstance
     */
    fun register(registration: R)

    /**
     * 从ServiceRegistry当中去取消注册一个实例
     *
     * @param registration 要取消注册的ServiceInstance
     */
    fun deregister(registration: R)

    /**
     * 关闭ServiceRegistry, 它是一个生命周期钩子方法
     */
    fun close()

    /**
     * 设置某个ServiceInstance的状态信息
     *
     * @param registration 目标ServiceInstance
     * @param status 要设置的状态
     */
    fun setStatus(registration: R, status: String)

    /**
     * 要获取某个ServiceInstance的状态信息
     *
     * @param registration 要获取信息的目标ServiceInstance
     * @return 该ServiceInstance的状态信息
     */
    fun <T> getStatus(registration: R): T
}