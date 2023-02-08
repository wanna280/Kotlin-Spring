package com.wanna.boot.actuate.endpoint.web.mvc

import com.wanna.boot.actuate.endpoint.web.EndpointLinksResolver
import com.wanna.boot.actuate.endpoint.web.EndpointMapping
import com.wanna.boot.actuate.endpoint.web.ExposableWebEndpoint
import com.wanna.boot.actuate.endpoint.web.Link
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.bind.annotation.ResponseBody
import com.wanna.framework.web.cors.CorsConfiguration
import com.wanna.framework.web.method.RequestMappingInfo
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import java.lang.reflect.Method

/**
 * WebMvc的Endpoint的HandlerMapping
 *
 * @param linksResolver 解析Endpoint的相关链接的处理器
 */
open class WebMvcEndpointHandlerMapping(
    endpoints: List<ExposableWebEndpoint>,
    endpointMapping: EndpointMapping,
    shouldRegisterLinksMapping: Boolean,
    val linksResolver: EndpointLinksResolver,
    @Nullable private val corsConfig: CorsConfiguration?
) : AbstractWebMvcEndpointHandlerMapping(endpoints, endpointMapping, shouldRegisterLinksMapping) {

    /**
     * 获取Links的Handler, 用于去进行暴露Actuator的相关接口到"/actuator"页面下
     *
     * @return 暴露Actuator相关URL的处理器
     */
    override fun getLinksHandler(): WebMvcLinksHandler = WebMvcLinksHandler()

    /**
     * WebMvc LinksHandler, 将Actuator相关接口的URL, 以RequestBody的方式去展示到页面上
     */
    protected inner class WebMvcLinksHandler : LinksHandler {
        @ResponseBody
        override fun links(request: HttpServerRequest, response: HttpServerResponse): Map<String, Map<String, Link>> {
            return mapOf("_links" to linksResolver.resolveLinks(request.getLocalHost() + request.getUri()))
        }

        override fun toString() = "Actuator root web endpoint"
    }

    /**
     * 判断是否有CorsConfigurationSource? (似乎也不必重写, 因为父类会根据MappingRegistry去进行匹配)
     * 因为这个HandlerMapping不会被配置全局的[CorsConfiguration], 因此我们不必使用父类的判断, 我们直接根据[corsConfig]去进行判断
     *
     * @param handler handler
     */
    override fun hasCorsConfigurationSource(handler: Any) = this.corsConfig != null

    /**
     * 告诉父类, 我当前方法的[CorsConfiguration]跨域的配置信息
     *
     * @param handler handler
     * @param mapping method
     * @param mapping
     * @return CorsConfiguration
     */
    @Nullable
    override fun initCorsConfiguration(handler: Any, method: Method, mapping: RequestMappingInfo) = this.corsConfig
}