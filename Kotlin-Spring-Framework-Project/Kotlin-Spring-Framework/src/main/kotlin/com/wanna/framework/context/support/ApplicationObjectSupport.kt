package com.wanna.framework.context.support

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import com.wanna.framework.context.ApplicationContextException

/**
 * 对于那些想要实现ApplicationContextAware的Application对象来说，这会是一个很好的父类；
 * 例如我们可以用来寻找一些用来协作的Bean，或者是访问ApplicationContext当中的特定的资源；
 * 它保存了ApplicationContext的引用并提供了初始化回调方法；
 *
 * 其实对于子类来说，这个作为父类并不是必须的，它只是让你可以更加容易地访问ApplicationContext；
 * 实际上很多地方并不一定需要ApplicationContext，因为它们接收Bean的引用；
 */
abstract class ApplicationObjectSupport : ApplicationContextAware {

    private var applicationContext: ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        if (this.applicationContext == null) {

            // 如果类型不匹配的话
            if (!requiredContextClass().isInstance(applicationContext)) {
                throw ApplicationContextException("需要一个ApplicationContext类型为[${this.requiredContextClass()}]，但是给定的ApplicationContext类型是[$applicationContext]")
            }
            this.applicationContext = applicationContext
            this.initApplicationContext(applicationContext)
        } else {
            if (this.applicationContext != applicationContext) {
                throw ApplicationContextException("ApplicationContext不能被重复设置成为不同的对象，之前是[${this.applicationContext}，之后是[$applicationContext]]")
            }
        }
    }

    /**
     * 是否ApplicationContext是必须设置的？如果是的话，没有设置还想获取直接抛异常
     */
    protected open fun isContextRequired(): Boolean {
        return false
    }

    /**
     * 需要的ApplicationContext的类型，当类型不匹配时，直接抛异常
     */
    protected open fun requiredContextClass(): Class<*> {
        return ApplicationContext::class.java
    }

    /**
     * 初始化ApplicationContext的回调方法
     *
     * @param applicationContext ApplicationContext
     */
    protected open fun initApplicationContext(applicationContext: ApplicationContext) {
        initApplicationContext()
    }

    protected open fun initApplicationContext() {

    }

    /**
     * 获取ApplicationContext，返回值可以为null(如果ApplicationContext不是必须的话)
     *
     * @throws ApplicationContextException 如果ApplicationContext必须存在，但是它没有被set
     */
    fun getApplicationContext(): ApplicationContext? {
        if (this.applicationContext == null && isContextRequired()) {
            throw ApplicationContextException("ApplicationContext是必须存在的，但是它没有被设置，获取失败")
        }
        return this.applicationContext
    }

    /**
     * 获取ApplicationContext，返回值不能为null(获取不到直接抛异常)
     *
     * @throws ApplicationContextException 如果获取ApplicationContext失败
     */
    fun obtainApplicationContext(): ApplicationContext {
        if (this.applicationContext == null) {
            throw ApplicationContextException("获取ApplicationContext失败，因为它没有被设置")
        }
        return this.applicationContext!!
    }
}