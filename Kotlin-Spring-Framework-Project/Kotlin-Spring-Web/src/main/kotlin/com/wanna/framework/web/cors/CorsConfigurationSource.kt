package com.wanna.framework.web.cors

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 基于给定的HttpServerRequest，去提供一个CorsConfiguration，维护了Cors的配置信息
 *
 * @see CorsConfiguration
 */
interface CorsConfigurationSource {

    /**
     * 针对指定的CorsConfiguration，去返回一个CorsConfiguration
     *
     * @param request request
     * @return CorsConfiguration
     */
    @Nullable
    fun getCorsConfiguration(request: HttpServerRequest): CorsConfiguration?
}