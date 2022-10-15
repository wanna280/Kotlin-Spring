package com.wanna.framework.web.config.annotation

import com.wanna.framework.web.cors.CorsConfiguration

/**
 * Cors(Cros Origin Resource Sharing)配置信息的注册中心
 */
open class CorsRegistry {

    // Cors配置的注册信息
    private val registrations = ArrayList<CorsRegistration>()

    /**
     * 添加一个Cors的Mapping
     *
     * @param pathPattern 要去进行匹配的模式
     * @return CorsRegistration
     */
    open fun addMapping(pathPattern: String): CorsRegistration {
        val corsRegistration = CorsRegistration(pathPattern)
        registrations += corsRegistration
        return corsRegistration
    }

    /**
     * 获取所有的CorsConfiguration配置信息
     *
     * @return CorsConfigurations(key-pathPattern, value-CorsConfiguration)
     */
    open fun getCorsConfigurations(): Map<String, CorsConfiguration> =
        registrations.associate { it.pathPattern to it.getCorsConfiguration() }
}