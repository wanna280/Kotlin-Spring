package com.wanna.boot.actuate.autoconfigure.endpoint.web

import com.wanna.boot.context.properties.ConfigurationProperties
import com.wanna.framework.web.cors.CorsConfiguration

/**
 * 对于Actuator当中的Endpoint的跨域配置信息
 */
@ConfigurationProperties("management.endpoints.web.cors")
open class CorsEndpointProperties {

    var allowedOrigins: List<String> = ArrayList()

    var allowedOriginPatterns: List<String> = ArrayList()

    var allowedMethods: List<String> = ArrayList()

    var allowedHeaders: List<String> = ArrayList()

    var exposedHeaders: List<String> = ArrayList()

    var allowCredentials: Boolean? = null

    var maxAge: Long = 1800L


    /**
     * 将当前的类当中的配置信息转换为SpringMVC的CorsConfiguration
     *
     * @return 转换得到的CorsConfiguration
     */
    open fun toCorsConfiguration(): CorsConfiguration {
        val configuration = CorsConfiguration()
        if (allowCredentials != null) {
            configuration.setAllowCredentials(allowCredentials!!)
        }
        configuration.setMaxAge(maxAge)
        if (allowedOrigins.isNotEmpty()) {
            configuration.setAllowedOrigins(allowedOrigins)
        }
        if (allowedOriginPatterns.isNotEmpty()) {
            configuration.setAllowedOriginPatterns(allowedOriginPatterns)
        }
        if (allowedHeaders.isNotEmpty()) {
            configuration.setAllowedHeaders(allowedHeaders)
        }
        if (exposedHeaders.isNotEmpty()) {
            configuration.setExposeHeaders(exposedHeaders)
        }
        if (allowedMethods.isNotEmpty()) {
            configuration.setAllowedMethods(allowedMethods)
        }
        return configuration
    }
}