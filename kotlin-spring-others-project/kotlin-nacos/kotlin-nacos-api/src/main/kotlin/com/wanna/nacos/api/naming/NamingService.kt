package com.wanna.nacos.api.naming

/**
 * NamingService
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
interface NamingService {

    /**
     * 在给定的serviceName对应的服务下, 去注册一个实例对象
     *
     * @param serviceName serviceName
     * @param ip ip
     * @param port port
     */
    fun registerInstance(serviceName: String, ip: String, port: Int)

    /**
     * 在给定的serviceName对应的服务下, 去注册一个实例对象
     *
     * @param serviceName serviceName
     * @param groupName groupName
     * @param ip ip
     * @param port port
     */
    fun registerInstance(serviceName: String, groupName: String, ip: String, port: Int)
}