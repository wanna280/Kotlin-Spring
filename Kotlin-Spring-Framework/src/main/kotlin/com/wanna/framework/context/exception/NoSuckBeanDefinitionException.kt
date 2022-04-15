package com.wanna.framework.context.exception

/**
 * 当前Bean的定义信息未找到异常
 */
class NoSuckBeanDefinitionException(override val message: String?, override val cause: Throwable?) :
    BeansException() {
    // 提供几个构造器的重载
    constructor() : this(null, null)
    constructor(message: String?) : this(message, null)
    constructor(cause: Throwable?) : this(null, cause)
}