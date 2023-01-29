package com.wanna.framework.core.annotation

import com.wanna.framework.core.NestedRuntimeException

/**
 * 如果注解当中的属性被不合法的配置时会抛出的异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/31
 */
open class AnnotationConfigurationException(message: String?, cause: Throwable?) :
    NestedRuntimeException(message, cause) {
    constructor(message: String) : this(message, null)
}