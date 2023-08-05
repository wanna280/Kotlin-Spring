package com.wanna.cloud.nacos.registry

/**
 * NacosRegistration的自定义化器, 支持去对NacosRegistration去完成自定义
 *
 * @author wanna
 */
fun interface NacosRegistrationCustomizer {

    /**
     * 自定义NacosRegistration的回调函数
     *
     * @param registration 要进行注册的NacosRegistration
     */
    fun customNacosRegistration(registration: NacosRegistration)
}