package com.wanna.framework.web.cors

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.AntPathMatcher
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 基于URL去进行匹配的CorsConfigurationSource，根据路径去判断已经注册的CorsConfigurations当中，
 * 是否存在有合适的CorsConfiguration，支持去处理当前的CORS请求
 *
 * @see CorsConfiguration
 * @see CorsConfigurationSource
 */
open class UrlBasedCorsConfigurationSource : CorsConfigurationSource {

    // PathMatcher，完成对路径的匹配
    private val pathMatcher = AntPathMatcher()

    // CorsConfiguration
    private val corsConfigurations = LinkedHashMap<String, CorsConfiguration>()

    /**
     * 获取CorsConfiguration，遍历已经注册的所有的CorsConfiguration，挨个去进行比较，
     * 判断是否有一个合适的CorsConfiguration支持去处理当前的CORS请求
     *
     * @param request
     * @return 如果找到了合适的CorsConfig，return CorsConfig；否则return null
     */
    @Nullable
    override fun getCorsConfiguration(request: HttpServerRequest): CorsConfiguration? {
        corsConfigurations.forEach { (pattern, config) ->
            if (pathMatcher.match(pattern, request.getUrl())) {
                return config
            }
        }
        return null
    }

    /**
     * 设置要使用的CorsConfiguration配置信息
     *
     * @param corsConfigurations CorsConfigurations
     */
    open fun setCorsConfigurations(corsConfigurations: Map<String, CorsConfiguration>) {
        this.corsConfigurations.clear()
        corsConfigurations.forEach(this.corsConfigurations::put)
    }
}