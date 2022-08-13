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
        if (CorsUtils.isCorsRequest(request)) {
            return EMPTY_CONDITION
        }
        if (this.isEmpty()) {
            return this
        }
        val mediaTypes: List<MediaType>?
        try {
            mediaTypes = getAcceptedMediaTypes(request) ?: return EMPTY_CONDITION
        } catch (ex: Exception) {
            return null
        }
        val result = getMatchingExpressions(mediaTypes)
        if (result != null && result.isNotEmpty()) {
            return ProducesRequestCondition(produces = result, manager = this.manager)
        }
        if (MediaType.ALL.isPresentIn(mediaTypes)) {
            return EMPTY_CONDITION
        }
        return null
    }

    // TODO
    private fun getMatchingExpressions(acceptedMediaTypes: List<MediaType>): List<String>? {
        return emptyList()
    }

    /**
     * 获取用户的可以接受的MediaType列表
     *
     * @param request request
     * @return 用户可以接受的MediaType列表
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