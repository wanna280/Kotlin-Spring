package com.wanna.framework.web.method

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.core.annotation.AnnotatedElementUtils
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
         * @param applicationContext ApplicationContext
         * @return 寻找到的ControllerAdviceBean
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

    private var beanOrBeanName: Any? = null

    private var beanFactory: BeanFactory? = null

    private var isSingleton = true

    constructor(bean: Any) : this() {
        this.beanOrBeanName = bean
        this.isSingleton = true
    }

    constructor(beanName: String, beanFactory: BeanFactory, controllerAdvice: ControllerAdvice? = null) : this() {
        this.beanFactory = beanFactory
        this.beanOrBeanName = beanName
        this.isSingleton = beanFactory.isSingleton(beanName)
    }

    open fun getBeanType(): Class<*> {
        if (this.beanOrBeanName is String) {
            return beanFactory!!.getType(this.beanOrBeanName as String)!!
        }
        return this.beanOrBeanName!!::class.java
    }

    open fun resolveBean(): Any {
        if (this.beanOrBeanName is String) {
            return beanFactory!!.getBean(this.beanOrBeanName as String)
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