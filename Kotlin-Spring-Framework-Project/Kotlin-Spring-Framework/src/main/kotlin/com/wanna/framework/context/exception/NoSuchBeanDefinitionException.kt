package com.wanna.framework.context.exception

/**
 * 当前Bean的定义信息未找到异常
 */
open class NoSuchBeanDefinitionException(
    message: String?,
    cause: Throwable?,
    beanName: String?,
    val beanType: Class<*>?
) :
    BeansException(message, cause, beanName) {

    /**
     * 针对于只提供message和exception提供构造器的重载
     */
    constructor(message: String?, cause: Throwable?) : this(message, cause, null, null)

    /**
     * 针对于只提供message的方式提供构造器的重载
     */
    constructor(message: String?) : this(message, null, null, null)

    /**
     * 针对于只提供cause和beanName的方式提供的构造器的重载
     */
    constructor(cause: Throwable?, beanName: String?) : this(null, cause, beanName, null)

    /**
     * 针对message和beanType的方式去进行提供构造
     */
    constructor(message: String?, beanType: Class<*>?) : this(message, null, null, beanType)

    /**
     * 针对于只传递message和beanName的方式提供构造器的重载
     */
    constructor(message: String?, beanName: String?) : this(message, null, beanName, null)
}