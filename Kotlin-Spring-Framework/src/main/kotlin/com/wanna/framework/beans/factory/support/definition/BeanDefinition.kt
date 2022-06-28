package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.beans.factory.support.definition.config.AttributeAccessor
import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataElement
import com.wanna.framework.beans.factory.config.ConstructorArgumentValues
import com.wanna.framework.beans.MutablePropertyValues
import com.wanna.framework.beans.method.MethodOverrides
import java.util.function.Supplier

/**
 * 提供对Bean的相关信息的管理，它提供的属性的
 */
interface BeanDefinition : AttributeAccessor, BeanMetadataElement {

    companion object {
        // Bean的作用域
        const val SCOPE_SINGLETON = "singleton"  // 单例的scope名称
        const val SCOPE_PROTOTYPE = "prototype"  // 原型的scope名称

        // Bean的角色，包括Application(默认)、Support和Infrastructure(基础设施)
        const val ROLE_APPLICATION = 0
        const val ROLE_SUPPORT = 1
        const val ROLE_INFRASTRUCTURE = 2
    }

    /**
     * beanClass
     */
    fun getBeanClass(): Class<*>?
    fun getBeanClassName() : String?
    fun setBeanClass(beanClass: Class<*>?);

    /**
     * 是否单例
     */
    fun isSingleton(): Boolean

    /**
     * 是否原型？
     */
    fun isPrototype(): Boolean

    /**
     * 是否是Autowire时需要进行优先注入的Bean
     */
    fun isPrimary(): Boolean
    fun setPrimary(primary: Boolean)

    /**
     * 是否是一个Autowire的候选Bean
     */
    fun isAutowireCandidate(): Boolean
    fun setAutowireCandidate(candidate: Boolean)

    /**
     * 设置创建Bean的工厂方法，也就是@Bean的方法
     */
    fun setFactoryMethodName(factoryMethodName: String?)
    fun getFactoryMethodName(): String?

    /**
     * 设置创建Bean的工厂方法对应的类的BeanName
     */
    fun setFactoryBeanName(factoryBeanName: String?)
    fun getFactoryBeanName(): String?

    /**
     * Bean的初始化回调方法
     */
    fun getInitMethodName(): String?
    fun setInitMethodName(initMethodName: String?)

    /**
     * Bean的摧毁的预先回调方法
     */
    fun setDestroyMethodName(destroyMethodName: String?)
    fun getDestroyMethodName(): String?

    /**
     * Bean的作用域，包括单例/原型等
     */
    fun setScope(scopeName: String)
    fun getScope(): String

    /**
     * 设置Bean的Role
     */
    fun setRole(role: Int)
    fun getRole(): Int

    /**
     * 是否是抽象的？
     */
    fun setAbstract(abstractFlag: Boolean)
    fun isAbstract(): Boolean

    /**
     * 设置实例化的Supplier
     */
    fun setInstanceSupplier(supplier: Supplier<*>?)
    fun getInstanceSupplier(): Supplier<*>?

    /**
     * 获取运行时方法重写的列表
     */
    fun getMethodOverrides(): MethodOverrides

    /**
     * 是否有需要进行运行时方法重写的？
     */
    fun hasMethodOverrides(): Boolean

    /**
     * 获取要进行设置的属性值
     */
    fun getPropertyValues(): MutablePropertyValues

    /**
     * 是否有PropertyValue？
     */
    fun hasPropertyValues(): Boolean

    /**
     * 获取构造器参数
     */
    fun getConstructorArgumentValues(): ConstructorArgumentValues

    /**
     * 是否有构造器参数？
     */
    fun hasConstructorArgumentValues(): Boolean

    /**
     * 是否懒加载
     */
    fun isLazyInit(): Boolean
    fun setLazyInit(lazyInit: Boolean)

    /**
     * 创建一个Bean时，它需要依赖的Bean的列表
     */
    fun setDependsOn(dependsOn: Array<String>)
    fun getDependsOn(): Array<String>

}