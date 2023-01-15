package com.wanna.boot.actuate.endpoint.web

import com.wanna.boot.actuate.endpoint.Operation

/**
 * Web环境下的Endpoint的Operation
 *
 * @see Operation
 */
interface WebOperation : Operation {
    /**
     * 获取OperationId
     *
     * @return operationId
     */
    fun getId(): String

    /**
     * 获取请求断言, 去对请求去进行匹配, 使用时, 需要转换为SpringMVC的RequestCondition
     *
     * @return WebOperationRequestPredicate
     * @see com.wanna.framework.web.mvc.condition.RequestCondition
     */
    fun getRequestPredicate(): WebOperationRequestPredicate
}