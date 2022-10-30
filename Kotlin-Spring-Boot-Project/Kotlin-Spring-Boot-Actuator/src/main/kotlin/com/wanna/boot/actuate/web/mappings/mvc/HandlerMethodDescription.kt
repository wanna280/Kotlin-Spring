package com.wanna.boot.actuate.web.mappings.mvc

import com.wanna.framework.web.method.HandlerMethod

/**
 * 对于SpringMVC当中的一个[HandlerMethod]的描述信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/31
 */
open class HandlerMethodDescription(handlerMethod: HandlerMethod) {

    /**
     * className
     */
    var className: String? = handlerMethod.method?.javaClass?.name

    /**
     * methodName
     */
    var name: String? = handlerMethod.method?.name

    /**
     * descriptor
     */
    var descriptor: String? = handlerMethod.method?.toGenericString()
}