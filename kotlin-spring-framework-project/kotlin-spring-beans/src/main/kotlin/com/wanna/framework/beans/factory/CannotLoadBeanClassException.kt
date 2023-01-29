package com.wanna.framework.beans.factory

import com.wanna.framework.beans.BeansException

/**
 * 无法完成BeanClass的类加载异常
 *
 * TODO
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/2
 */
open class CannotLoadBeanClassException(message: String?, cause: Throwable?) : BeansException(message, cause) {
    constructor(message: String) : this(message, null)
}