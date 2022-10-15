package com.wanna.framework.context.exception

/**
 * Bean创建过程中发生的异常
 */
open class BeanCreationException(message: String?, cause: Throwable?, beanName: String?) :
    BeansException(message, cause, beanName) {
    /**
     * 针对于只提供message和exception提供构造器的重载
     */
    constructor(message: String?, cause: Throwable?) : this(message, cause, null)

    /**
     * 针对于只提供message的方式提供构造器的重载
     */
    constructor(message: String?) : this(message, null, null)

    /**
     * 针对于只提供cause和beanName的方式提供的构造器的重载
     */
    constructor(cause: Throwable?, beanName: String?) : this(null, cause, beanName)

    /**
     * 针对于只传递message和beanName的方式提供构造器的重载
     */
    constructor(message: String?, beanName: String?) : this(message, null, beanName)
}