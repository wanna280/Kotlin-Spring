package com.wanna.framework.context.exception

/**
 * 在容器中无法找到唯一的BeanDefinition异常，在进行Autowire注入时，如果出现要注入的Bean的数量不唯一时，就会抛出BeanDefinition不唯一的异常
 */
class NoUniqueBeanDefinitionException(override val message: String?, override val cause: Throwable?) :
    BeansException(message, cause) {
    // 提供几个构造器的重载
    constructor() : this(null, null)
    constructor(message: String?) : this(message, null)
    constructor(cause: Throwable?) : this(null, cause)
}