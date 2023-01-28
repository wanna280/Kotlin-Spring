package com.wanna.framework.web.mvc.condition

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.server.HttpServerRequest

/**
 * Server端支持去进行处理的MediaType的匹配, 用于去匹配HttpHeaders当中的"Content-Type"
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 *
 * @param consumes Serve端的HandlerMethod支持去进行处理的[MediaType]类型
 */
open class ConsumesRequestCondition(consumes: List<String>) :
    AbstractRequestCondition<ConsumesRequestCondition>() {

    /**
     * 支持去进行处理的[MediaType]列表
     */
    private val consumes: List<MediaType> = MediaType.parseMediaTypes(consumes)

    /**
     * 提供一个变长参数的构造器
     *
     * @param consumes consumes
     */
    constructor(vararg consumes: String) : this(consumes.toList())

    /**
     * 获取content
     *
     * @return consumes
     */
    override fun getContent(): Collection<*> = consumes

    /**
     * 多个之间的关系是或, 因此使用"||"
     *
     * @return toString分隔符
     */
    override fun getToStringInfix(): String = " || "

    /**
     * 联合另外一个[ConsumesRequestCondition]
     *
     * @param other other
     * @return 联合之后得到的[ConsumesRequestCondition]
     */
    override fun combine(other: ConsumesRequestCondition): ConsumesRequestCondition {
        if (this.isEmpty()) {
            return other
        }
        if (other.isEmpty()) {
            return other
        }

        // 如果两个都不为空的话, 那么使用后给的为准
        return other
    }

    /**
     * 执行对于request的匹配
     *
     * @param request request
     * @return 如果匹配失败return null
     */
    @Nullable
    override fun getMatchingCondition(request: HttpServerRequest): ConsumesRequestCondition? {
        if (consumes.isEmpty()) {
            return this
        }

        val contentType = request.getHeaders().getContentType()
        if (contentType === null) {
            return null
        }

        for (consume in consumes) {
            if (consume.isCompatibleWith(contentType)) {
                return this
            }
        }

        return null
    }
}