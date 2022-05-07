package com.wanna.framework.context.exception

/**
 * 在容器中无法找到唯一的BeanDefinition异常，在进行Autowire注入时，如果出现要注入的Bean的数量不唯一时，就会抛出BeanDefinition不唯一的异常
 */
class NoUniqueBeanDefinitionException(message: String?, cause: Throwable?, beanName: String?) :
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