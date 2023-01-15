package com.wanna.framework.web.mvc.condition

import com.wanna.framework.web.server.HttpServerRequest

/**
 * 封装了@RequestMapping的一些请求的匹配条件
 */
interface RequestCondition<T> {

    /**
     * combine, 需要联合别的RequestCondition; 
     * 比如, 方法上有RequestMapping, 类上也有RequestCondition, 就需要去进行combine
     *
     * @param other 别的RequestCondition
     */
    fun combine(other: T): T

    /**
     * 获取匹配结果
     *
     * @param request request
     * @return 如果匹配, return RequestCondition, 如果不匹配; return null
     */
    fun getMatchingCondition(request: HttpServerRequest): T?
}