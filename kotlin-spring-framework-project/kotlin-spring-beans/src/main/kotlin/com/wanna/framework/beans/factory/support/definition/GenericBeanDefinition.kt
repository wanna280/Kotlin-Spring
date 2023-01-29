package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.lang.Nullable

/**
 * 这是一个通用的BeanDefinition, 它主要为parent的BeanDefinition的动态设置提供支持
 *
 * @see AbstractBeanDefinition
 */
open class GenericBeanDefinition() : AbstractBeanDefinition() {
    constructor(@Nullable beanClass: Class<*>?) : this() {
        this.setBeanClass(beanClass)
    }

    /**
     * parent BeanDefinition的名字
     */
    @Nullable
    var parent: String? = null
}