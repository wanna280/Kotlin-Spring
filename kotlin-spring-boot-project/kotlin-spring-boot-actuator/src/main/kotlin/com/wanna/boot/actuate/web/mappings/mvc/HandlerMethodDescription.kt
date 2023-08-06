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
     * className(Handler方法所在的类名)
     */
    var className: String? = handlerMethod.method?.declaringClass?.name

    /**
     * methodName(Handler方法的方法名)
     */
    var name: String? = handlerMethod.method?.name

    /**
     * descriptor(Handler方法的签名信息)
     */
    var descriptor: String? = handlerMethod.method?.toGenericString()
}