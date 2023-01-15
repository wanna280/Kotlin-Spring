package com.wanna.framework.web.config.annotation

import com.wanna.framework.web.cors.CorsConfiguration

/**
 * 提供方便去进行构建一个CorsConfiguration的CorsRegistration
 *
 * @see CorsConfiguration
 * @see CorsRegistry
 *
 * @param pathPattern 对请求当中的哪些路径允许CROS的跨域请求？
 */
open class CorsRegistration(val pathPattern: String) {

    // 构建一个默认的允许跨域的方式的CORS配置信息,
    // 默认情况下是：放行所有的Header/Origin, 并配置允许的请求方式为"GET"/"POST"/"HEAD"三种类型
    private val config = CorsConfiguration().applyPermitDefaultValues()

    /**
     * 它允许哪些Origin去进行CORS操作？不支持使用表达式.如果想要使用表达式, 那么需要使用"allowedOriginPatterns"方法
     *
     * @param origins 你想配置的允许进行CORS的Origin, 可以为"*"通配(默认)
     * @see allowedOriginPatterns
     */
    open fun allowedOrigins(vararg origins: String): CorsRegistration {
        config.setAllowedOrigins(listOf(*origins))
        return this
    }

    /**
     * 设置跨域允许的请求头, 可以为通配"*"(默认), 预检"PreFlight"请求当中, 必须携带指定的"Header"(至少一个)才允许该CORS请求
     *
     * @param allowedHeaders 允许的Header
     */
    open fun allowedHeaders(vararg allowedHeaders: String): CorsRegistration {
        config.setAllowedHeaders(listOf(*allowedHeaders))
        return this
    }

    /**
     * 设置跨域允许的请求方法, 可以为"GET"/"POST"/"HEAD"/"DELETE"/"PATCH"等, 也可以是通配"*"通配,
     * 如果不是你给定的这些方法, 那么该CORS请求不被允许
     *
     * @param allowedMethods 你想要允许的CORS的请求方式
     */
    open fun allowedMethods(vararg allowedMethods: String): CorsRegistration {
        config.setAllowedMethods(listOf(*allowedMethods))
        return this
    }

    /**
     * 设置允许跨域的Origin的表达式, 如果不需要使用表达式, 直接指定Origin, 可以使用"allowedOrigins"方法
     *
     * @param allowOriginPatterns 想要去进行匹配的表达式, 只有Origin符合表达式时, 才允许该CORS请求
     * @see allowedOrigins
     */
    open fun allowedOriginPatterns(vararg allowOriginPatterns: String): CorsRegistration {
        config.setAllowedOriginPatterns(listOf(*allowOriginPatterns))
        return this
    }

    /**
     * 设置CORS的最大存活时间, 超过这个时间将会重新发送预检请求(单位为s, 默认值为1800L, 也就是30min)
     *
     * @param maxAge 你想要使用的Cors的最大存活时间
     */
    open fun maxAge(maxAge: Long): CorsRegistration {
        config.setMaxAge(maxAge)
        return this
    }

    /**
     * set AllowCredentials
     *
     * @param allowCredentials allowCredentials
     */
    open fun setAllowCredentials(allowCredentials: Boolean): CorsRegistration {
        config.setAllowCredentials(allowCredentials)
        return this
    }

    /**
     * 设置CORS请求应该去进行暴露的请求头, 需要这些将RequestHeader写入到"Response.Header"当中
     *
     * @param exposeHeaders 你想要去进行暴露到Response当中的RequestHeader
     */
    open fun setExposeHeaders(vararg exposeHeaders: String): CorsRegistration {
        config.setExposeHeaders(listOf(*exposeHeaders))
        return this
    }

    /**
     * 获取当前CorsRegistration去进行构建好的CORS的CorsConfiguration对象
     *
     * @return CorsConfiguration
     */
    open fun getCorsConfiguration(): CorsConfiguration = this.config
}