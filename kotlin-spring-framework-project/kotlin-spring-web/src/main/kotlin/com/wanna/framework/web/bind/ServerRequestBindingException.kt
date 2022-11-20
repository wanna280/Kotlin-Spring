package com.wanna.framework.web.bind

/**
 * 属性绑定失败异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
open class ServerRequestBindingException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)