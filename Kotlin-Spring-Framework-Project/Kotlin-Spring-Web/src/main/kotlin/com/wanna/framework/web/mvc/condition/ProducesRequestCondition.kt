package com.wanna.framework.web.mvc.condition

import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.bind.annotation.RequestMapping
import com.wanna.framework.web.context.request.ServerWebRequest
import com.wanna.framework.web.cors.CorsUtils
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 产出具体的MediaType的RequestCondition
 *
 * @see RequestMapping.produces
 *
 * @param produces 要产出的MediaTypes
 * @param manager 要使用的ContentNegotiationManager，有默认值
 */
open class ProducesRequestCondition(
    private val produces: List<String> = emptyList(),
    private val manager: ContentNegotiationManager = DEFAULT_CONTENT_NEGOTIATION_MANAGER
) : AbstractRequestCondition<ProducesRequestCondition>() {

    companion object {
        // Default ContentNegotiationManager
        @JvmStatic
        val DEFAULT_CONTENT_NEGOTIATION_MANAGER: ContentNegotiationManager = ContentNegotiationManager()

        // MediaType的属性名
        @JvmStatic
        private val MEDIA_TYPES_ATTRIBUTE = ProducesRequestCondition::class.java.name + ".MEDIA_TYPES"

        // 空的ProducesCondition
        @JvmStatic
        private val EMPTY_CONDITION = ProducesRequestCondition()
    }

    // 提供一个变长参数的构造方法
    constructor(vararg produces: String) : this(produces.toList(), DEFAULT_CONTENT_NEGOTIATION_MANAGER)

    override fun getContent() = this.produces

    /**
     * 获取可以产出的MediaType
     *
     * @return 可以产出的MediaType列表
     */
    open fun getProducibleMediaTypes(): Set<MediaType> = this.produces.map { MediaType.parseMediaType(it) }.toSet()

    override fun getToStringInfix() = " || "

    /**
     * combine的方式为，如果other当中存在有配置，那么就使用它的配置，否则就使用当前的配置；
     * 典型的就是：当方法级别上的配置，会替换掉类上的配置(而不是真的对两个方法当中的产出类型去进行合并)
     *
     * @param other other Produces，例如之前是类级别的匹配，现在是方法级别的匹配
     */
    override fun combine(other: ProducesRequestCondition) = if (other.getContent().isNotEmpty()) other else this

    /**
     * 获取匹配的结果
     *
     * @param request request
     * @return 匹配的结果，如果不匹配的话，return null；否则返回的是正常的ProducesRequestCondition
     */
    override fun getMatchingCondition(request: HttpServerRequest): ProducesRequestCondition? {
        // 1.如果它是一个Cors的预检请求("OPTIONS")，那么return EMPTY_CONDITION, 匹配
        if (CorsUtils.isPreFlightRequest(request)) {  // fixed for PreFlightRequest
            return EMPTY_CONDITION
        }

        // 2.如果当前Produces为空，没有要去进行产出的类型，那么return this, 匹配
        if (this.isEmpty()) {
            return this
        }

        // 3.尝试去解析客户端想要去进行接收的MediaType列表
        // --3.1 先尝试从request的属性当中去进行探索
        // --3.2 再尝试根据ContentNegotiationManager当中去进行解析
        val mediaTypes: List<MediaType>?
        try {
            mediaTypes = getAcceptedMediaTypes(request) ?: return null
        } catch (ex: Exception) {
            return null  // 解析错误，那么说明不匹配，return null
        }

        // 4.获取匹配的结果，判断你想要去进行接收的MediaType，看下有哪个是我能产出的？
        val result = getMatchingExpressions(mediaTypes)

        // 5.如果确实有我能去进行产出的，那么return ProducesRequestCondition，并包装我能去进行产出的类型
        if (result != null && result.isNotEmpty()) {
            return ProducesRequestCondition(produces = result, manager = this.manager)
        }

        // 6.如果包含了ALL，那么return EMPTY_CONDITION
        if (MediaType.ALL.isPresentIn(mediaTypes)) {
            return EMPTY_CONDITION
        }
        return null
    }

    /**
     * 遍历你所有想要接收的数据类型，看是否有一个是我想要去进行产出的？
     *
     * @param acceptedMediaTypes 你想要去进行接收的MediaType列表
     * @return 如果有我能去进行产出的，那么return 能产出MediaType的List；如果没有我能去进行产出的，那么return null
     */
    private fun getMatchingExpressions(acceptedMediaTypes: List<MediaType>): List<String>? {
        val result = ArrayList<String>()
        produces.forEach {
            val mediaType = MediaType.parseMediaType(it)
            acceptedMediaTypes.forEach { accept ->
                if (mediaType.isCompatibleWith(accept)) {
                    // 如果匹配的话，应该使用的是用户自己的去配置的，而不是用户想要去进行接收的
                    // 例如用户配置了"application/json"，而Accept为"*/*"，此时不应该返回"*/*"...
                    result += it
                }
            }
        }
        return result.ifEmpty { null }  // if empty return null, else return result
    }

    /**
     * 根据ContentNegotiationManager去获取用户的可以接受的MediaType列表
     *
     * @param request request
     * @return 从request当中去解析到的用户可以接受的MediaType列表
     */
    @Suppress("UNCHECKED_CAST")
    private fun getAcceptedMediaTypes(request: HttpServerRequest): List<MediaType>? {
        var result = request.getAttribute(MEDIA_TYPES_ATTRIBUTE)
        if (result == null) {
            result = this.manager.resolveMediaTypes(ServerWebRequest(request))
            request.setAttribute(MEDIA_TYPES_ATTRIBUTE, result)
        }
        return result as List<MediaType>?
    }
}