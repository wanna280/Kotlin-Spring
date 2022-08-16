package com.wanna.framework.validation.beanvalidation

import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
import com.wanna.framework.context.ApplicationContext
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorFactory

/**
 * Spring基于JSR303的ConstraintValidatorFactory的实现，基于内部去Delegate
 * 一个Spring的BeanFactory的方式去实现创建一个`ConstraintValidator`
 *
 * 这个类当中主要是提供编程式的API的方式去进行使用
 *
 * @author wanna
 * @see ApplicationContext.getAutowireCapableBeanFactory
 * @see AutowireCapableBeanFactory.createBean
 */
open class SpringConstraintValidatorFactory(val beanFactory: AutowireCapableBeanFactory) : ConstraintValidatorFactory {
    override fun <T : ConstraintValidator<*, *>?> getInstance(key: Class<T>): T = beanFactory.getBean(key)
    override fun releaseInstance(instance: ConstraintValidator<*, *>) = beanFactory.destroy(instance)
}