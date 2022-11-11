package com.wanna.boot.actuate.web.mappings.mvc

import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.method.RequestMappingInfo

/**
 * 对于SpringMVC当中的一个[RequestMappingInfo]去进行描述
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/31
 */
open class RequestMappingConditionsDescription(private val requestMappingInfo: RequestMappingInfo) {

    /**
     * 支持的请求方式
     */
    val methods: Set<RequestMethod> = requestMappingInfo.methodsCondition.methods

    /**
     * 需要携带的header列表
     */
    val headers: List<String> = ArrayList(requestMappingInfo.headersCondition.headers)

    /**
     * 获取支持去进行处理路径表达式列表
     */
    val patterns: Set<String> = requestMappingInfo.getPaths()

    /**
     * 产出的MediaType
     */
    val produces: List<MediaTypeExpressionDescription> =
        requestMappingInfo.producesCondition.getProducibleMediaTypes().map { MediaTypeExpressionDescription(it) }
            .toList()

    /**
     * 参数列表
     */
    val params: List<String> = requestMappingInfo.paramsCondition.params

    /**
     * MediaType的描述信息
     */
    class MediaTypeExpressionDescription(_mediaType: MediaType) {
        var mediaType: String = _mediaType.toString()
        var negated: Boolean = false
    }

    /**
     * Name-Value的表达式的描述信息
     */
    class NameValueExpressionDescription {

    }

}