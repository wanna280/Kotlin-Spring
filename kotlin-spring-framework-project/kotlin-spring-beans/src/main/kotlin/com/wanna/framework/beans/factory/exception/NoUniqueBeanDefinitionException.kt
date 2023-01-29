package com.wanna.framework.beans.factory.exception

import com.wanna.framework.beans.BeansException

/**
 * 在容器中无法找到唯一的BeanDefinition异常, 在进行Autowire注入时, 如果出现要注入的Bean的数量不唯一时, 就会抛出BeanDefinition不唯一的异常
 */
open class NoUniqueBeanDefinitionException(message: String?, cause: Throwable?, val beanName: String?) :
    BeansException(message, cause) {
    constructor(message: String?, cause: Throwable?) : this(message, cause, null)
    constructor(message: String?) : this(message, null, null)
}