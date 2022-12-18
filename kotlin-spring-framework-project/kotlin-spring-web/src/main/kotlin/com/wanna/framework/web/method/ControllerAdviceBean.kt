package com.wanna.framework.web.method

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.bind.annotation.ControllerAdvice

/**
 * ControllerAdviceBean，它描述的是一个标注了@ControllerAdvice的Bean
 *
 * @see ControllerAdvice
 */
open class ControllerAdviceBean() {

    companion object {
        /**
         * 从容器当中找到所有标注了@ControllerAdvice的Bean，封装成为ControllerAdviceBean
         *
         * @param applicationContext 待寻找候选的Bean的ApplicationContext
         * @return 从给定的ApplicationContext寻找到的ControllerAdviceBean
         */
        @JvmStatic
        fun findAnnotatedBeans(applicationContext: ApplicationContext): List<ControllerAdviceBean> {
            val result = ArrayList<ControllerAdviceBean>()
            val beanNames = applicationContext.getBeanNamesForTypeIncludingAncestors(Any::class.java, true, false)
            beanNames.forEach { name ->
                val type = applicationContext.getType(name)
                if (type != null && AnnotatedElementUtils.isAnnotated(type, ControllerAdvice::class.java)) {
                    result += ControllerAdviceBean(name, applicationContext)
                }
            }
            return result
        }
    }

    /**
     * BeanObject or beanName
     */
    private var beanOrBeanName: Any? = null

    /**
     * beanType
     */
    private var beanType: Class<*>? = null

    /**
     * beanFactory, 当beanOrBeanName为字符串时需要使用到
     */
    @Nullable
    private var beanFactory: BeanFactory? = null

    /**
     * 是否是单例的?
     */
    private var isSingleton = true

    constructor(bean: Any) : this() {
        this.beanOrBeanName = bean
        this.isSingleton = true
        this.beanType = ClassUtils.getUserClass(bean)
    }

    constructor(beanName: String, beanFactory: BeanFactory, controllerAdvice: ControllerAdvice? = null) : this() {
        this.beanFactory = beanFactory
        this.beanOrBeanName = beanName
        this.isSingleton = beanFactory.isSingleton(beanName)

        // must be origin user class
        this.beanType = ClassUtils.getUserClass(beanFactory.getType(beanName)!!)
    }

    /**
     * 获取到beanType
     *
     * @return beanTupe
     */
    open fun getBeanType(): Class<*> = this.beanType!!

    /**
     * 解析Bean(如果之前是一个字符串的话，那么我们需要把它当做beanName，从BeanFactory当中获取)
     *
     * @return 解析到的BeanObject
     */
    open fun resolveBean(): Any {
        if (this.beanOrBeanName is String) {
            return beanFactory?.getBean(this.beanOrBeanName as String)
                ?: throw IllegalStateException("当bean为字符串时, BeanFactory不能为null")
        }
        return this.beanOrBeanName!!
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ControllerAdviceBean
        if (beanOrBeanName != other.beanOrBeanName) return false
        if (beanFactory != other.beanFactory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = beanOrBeanName?.hashCode() ?: 0
        result = 31 * result + (beanFactory?.hashCode() ?: 0)
        return result
    }


}