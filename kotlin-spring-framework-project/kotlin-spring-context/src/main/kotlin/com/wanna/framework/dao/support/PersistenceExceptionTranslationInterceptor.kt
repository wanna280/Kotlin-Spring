package com.wanna.framework.dao.support

import com.wanna.framework.aop.intercept.MethodInterceptor
import com.wanna.framework.aop.intercept.MethodInvocation
import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.beans.factory.ListableBeanFactory

/**
 * 持久层的异常翻译器的拦截器, 拦截目标方法, 为目标方法去
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 * @param exceptionTranslator 持久层的异常翻译器
 * @param beanFactory  BeanFactory
 */
open class PersistenceExceptionTranslationInterceptor private constructor(
    private var exceptionTranslator: PersistenceExceptionTranslator? = null,
    private var beanFactory: ListableBeanFactory? = null
) : MethodInterceptor, BeanFactoryAware, InitializingBean {

    /**
     * 提供一个无参数构造器, 因为可以通过BeanFactoryAware自动注入BeanFactory
     */
    constructor() : this(null, null)

    /**
     * 提供一个基于BeanFactory的构造器
     */
    constructor(beanFactory: ListableBeanFactory) : this(null, beanFactory)

    /**
     * 提供基于直接给定PersistenceExceptionTranslator的构造器
     */
    constructor(exceptionTranslator: PersistenceExceptionTranslator) : this(exceptionTranslator, null)

    override fun setBeanFactory(beanFactory: BeanFactory) {
        if (this.exceptionTranslator == null) {
            if (this.beanFactory !is ListableBeanFactory) {
                throw IllegalStateException("不支持使用非ListableBeanFactory去探测PersistenceExceptionTranslator")
            }
            this.beanFactory = beanFactory as ListableBeanFactory
        }
    }

    override fun afterPropertiesSet() {
        if (this.exceptionTranslator == null && this.beanFactory == null) {
            throw IllegalStateException("BeanFactory和PersistenceExceptionTranslator必须指定一个, 不允许两个都不指定")
        }
    }

    override fun invoke(invocation: MethodInvocation): Any? {
        try {
            return invocation.proceed()
        } catch (ex: RuntimeException) {
            if (this.exceptionTranslator == null) {
                this.exceptionTranslator = detectPersistenceExceptionTranslators(this.beanFactory!!)
            }
            if (this.exceptionTranslator != null) {
                throw DataAccessUtils.translateIfNecessary(ex, exceptionTranslator!!)
            }
            throw ex
        }
    }

    /**
     * 从BeanFactory当中去进行探测所有的[PersistenceExceptionTranslator]
     *
     * @param beanFactory BeanFactory
     * @return 从BeanFactory当中探测到的所有的[PersistenceExceptionTranslator]的组合
     */
    protected open fun detectPersistenceExceptionTranslators(beanFactory: ListableBeanFactory): PersistenceExceptionTranslator {
        val exceptionTranslator = ChainedPersistenceExceptionTranslator()
        val exceptionTranslators =
            beanFactory.getBeansForTypeIncludingAncestors(PersistenceExceptionTranslator::class.java)
        exceptionTranslators.values.forEach(exceptionTranslator::addDelegate)
        return exceptionTranslator
    }
}