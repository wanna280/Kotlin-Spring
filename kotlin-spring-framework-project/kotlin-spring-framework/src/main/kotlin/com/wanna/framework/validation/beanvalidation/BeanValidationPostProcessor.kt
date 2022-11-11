package com.wanna.framework.validation.beanvalidation

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.context.processor.beans.BeanPostProcessor

/**
 * 对Spring的Bean去提供参数的检验的BeanPostProcessor
 *
 * @see BeanPostProcessor
 */
open class BeanValidationPostProcessor : BeanPostProcessor, InitializingBean {
    override fun afterPropertiesSet() {

    }
}