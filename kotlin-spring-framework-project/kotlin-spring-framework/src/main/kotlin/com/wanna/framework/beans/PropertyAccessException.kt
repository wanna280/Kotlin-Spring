package com.wanna.framework.beans

import java.beans.PropertyChangeEvent

/**
 * 在进行属性的访问时, 出现了异常, 例如执行目标方法异常、字段类型不匹配异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/20
 */
open class PropertyAccessException(val propertyChangeEvent: PropertyChangeEvent?, message: String?, cause: Throwable?) :
    BeansException(message, cause) {
    constructor(message: String?, cause: Throwable?) : this(null, message, cause)
    constructor(message: String?) : this(null, message, null)
}