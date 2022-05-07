package com.wanna.framework.context.exception

/**
 * 声明在创建Bean过程中发生的异常
 */
open class BeansException(override val message: String?, override val cause: Throwable?, val beanName: String?) :
    RuntimeException() {
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

    override fun toString(): String {
        return "[$javaClass](message=$message, cause=$cause, beanName=$beanName)"
    }
}