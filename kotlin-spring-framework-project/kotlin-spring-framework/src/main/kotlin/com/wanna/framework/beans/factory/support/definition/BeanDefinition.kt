package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.beans.MutablePropertyValues
import com.wanna.framework.beans.factory.config.ConstructorArgumentValues
import com.wanna.framework.beans.factory.support.definition.config.AttributeAccessor
import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataElement
import com.wanna.framework.beans.method.MethodOverrides
import com.wanna.framework.lang.Nullable
import java.util.function.Supplier

/**
 * 提供对Bean的相关信息的管理，它提供的属性的访问功能，以及Source的访问的相关功能
 *
 * * 1.属性的访问，可以提供一个上下文的环境，因为Spring当中，无论在哪都能获取BeanDefinition，
 * 因此将属性设置到BeanDefinition当中，可以当做全局变量去进行访问
 *
 * @see AbstractBeanDefinition
 * @see RootBeanDefinition
 * @see GenericBeanDefinition
 * @see AttributeAccessor
 */
interface BeanDefinition : AttributeAccessor, BeanMetadataElement {
    companion object {
        // Bean的作用域
        const val SCOPE_SINGLETON = "singleton"  // 单例的scope名称
        const val SCOPE_PROTOTYPE = "prototype"  // 原型的scope名称

        // Bean的角色，包括Application(默认)、Support和Infrastructure(基础设施)
        const val ROLE_APPLICATION = 0  // 正常的用户使用的Bean
        const val ROLE_SUPPORT = 1
        const val ROLE_INFRASTRUCTURE = 2  // 基础设施Bean，不用被使用者所知道的Bean
    }

    /**
     * 获取当前BeanDefinition的beanClass
     *
     * @return beanClass(如果不存在return null)
     */
    @Nullable
    fun getBeanClass(): Class<*>?

    /**
     * 获取beanClassName
     *
     * @return beanClassName
     */
    @Nullable
    fun getBeanClassName(): String?

    /**
     * 设置beanClass
     *
     * @param beanClass beanClass
     */
    fun setBeanClass(@Nullable beanClass: Class<*>?);

    /**
     * 当前BeanDefinition对应的Bean是否是一个单例对象
     *
     * @return 如果BeanDefinition的scope为"singleton", 那么return true; 否则return false
     */
    fun isSingleton(): Boolean

    /**
     * 当前BeanDefinition对应的Bean是否是一个原型对象
     *
     * @return 如果BeanDefinition的scope为"prototype", 那么return true; 否则return false
     */
    fun isPrototype(): Boolean

    /**
     * 是否是Autowire时需要进行优先注入的Bean
     *
     * @return 如果是需要去进行优先注入的Bean, return true; 否则return false
     */
    fun isPrimary(): Boolean

    /**
     * 设置当前BeanDefinition是否是一个需要去进行优先注入的Bean的标志
     *
     * @param primary 如果需要设置为primary, 设置为true; 否则设置为false
     */
    fun setPrimary(primary: Boolean)

    /**
     * 是否是一个Autowire的候选Bean
     *
     * @return 如果它是一个Autowire的候选Bean, return true; 否则return false
     */
    fun isAutowireCandidate(): Boolean

    /**
     * 设置当前BeanDefinition是否是一个候选的去进行Autowire的Bean
     *
     * @param candidate 是否是一个优先去进行
     */
    fun setAutowireCandidate(candidate: Boolean)

    /**
     * 设置创建Bean的工厂方法，也就是@Bean的方法
     *
     * @param factoryMethodName factoryMethodName
     */
    fun setFactoryMethodName(@Nullable factoryMethodName: String?)

    /**
     * 获取创建Bean的工厂方法名, 也就是@Bean方法的方法名
     *
     * @return factoryMethodName
     */
    @Nullable
    fun getFactoryMethodName(): String?

    /**
     * 设置创建Bean的工厂方法对应的类的BeanName
     *
     * @param factoryBeanName factoryBeanName
     */
    fun setFactoryBeanName(@Nullable factoryBeanName: String?)

    /**
     * 获取到创建Bean的工厂方法对应的类的BeanName
     *
     * @return factoryBeanName
     */
    @Nullable
    fun getFactoryBeanName(): String?

    /**
     * 设置Bean的初始化回调方法
     *
     * @param initMethodName 需要使用的初始化方法
     */
    fun setInitMethodName(@Nullable initMethodName: String?)

    /**
     * 获取到当前Bean的初始化方法
     *
     * @return initMethodName
     */
    @Nullable
    fun getInitMethodName(): String?

    /**
     * 设置Bean的摧毁的预先回调方法
     *
     * @param destroyMethodName destroyMethodName
     */
    fun setDestroyMethodName(@Nullable destroyMethodName: String?)

    /**
     * 获取到当前Bean的摧毁的预先回调方法
     *
     * @return destroyMethodName
     */
    @Nullable
    fun getDestroyMethodName(): String?

    /**
     * Bean的作用域，包括单例(singleton)/原型(prototype)等
     *
     * @param scopeName scopeName
     */
    fun setScope(scopeName: String)

    /**
     * 获取BeanDefinition的Scope对应的scopeName
     *
     * @return scopeName
     */
    fun getScope(): String

    /**
     * 设置Bean的Role
     *
     * @param role role
     */
    fun setRole(role: Int)

    /**
     * 获取当前BeanDefinition的Role
     *
     * @return role
     */
    fun getRole(): Int

    /**
     * 当前BeanDefinition是否是抽象的？
     *
     * @param abstractFlag 如果抽象, 设置为true; 否则为false
     */
    fun setAbstract(abstractFlag: Boolean)

    /**
     * 当前BeanDefinition是否抽象的
     *
     * @return 如果抽象, return true; 否则return false
     */
    fun isAbstract(): Boolean

    /**
     * 设置实例化的Supplier
     *
     * @param supplier 用于去进行实例化的Supplier
     */
    fun setInstanceSupplier(@Nullable supplier: Supplier<*>?)

    /**
     * 获取用来进行实例化的Supplier
     *
     * @return 实例化的Supplier(不存在return null)
     */
    @Nullable
    fun getInstanceSupplier(): Supplier<*>?

    /**
     * 获取运行时方法重写的列表
     *
     * @return 当前BeanDefinition当中需要去进行运行时方法重写的方法列表
     */
    fun getMethodOverrides(): MethodOverrides

    /**
     * 当前BeanDefinition当中是否有需要进行运行时方法重写的？
     *
     * @return 如果当前BeanDefinition存在有要进行运行时方法重写的话, return true; 否则return false
     */
    fun hasMethodOverrides(): Boolean

    /**
     * 获取要进行设置的属性值列表
     *
     * @return 当前BeanDefinition当中的PropertyValue列表
     */
    fun getPropertyValues(): MutablePropertyValues

    /**
     * 是否有PropertyValue？
     *
     * @return 如果存在有PropertyValue的话, return true; 否则return false
     */
    fun hasPropertyValues(): Boolean

    /**
     * 获取当前BeanDefinition的构造器参数
     *
     * @return 当前BeanDefinition的构造器参数列表
     */
    fun getConstructorArgumentValues(): ConstructorArgumentValues

    /**
     * 当前BeanDefinition是否有指定构造器参数?
     *
     * @return 如果有指定构造器参数的话, return true; 否则return false
     */
    fun hasConstructorArgumentValues(): Boolean

    /**
     * 该Bean是否应该被懒加载
     *
     * @return 如果应该去进行懒加载, return true; 否则return false
     */
    fun isLazyInit(): Boolean

    /**
     * 设置该Bean是否应该去进行懒加载?
     *
     * @param lazyInit lazyInit
     */
    fun setLazyInit(lazyInit: Boolean)

    /**
     * 获取BeanDefinition的资源描述信息
     *
     * @return BeanDefinition资源描述信息(可以为null)
     */
    @Nullable
    fun getResourceDescription(): String?

    /**
     * 创建一个Bean时，它需要依赖的Bean的列表
     *
     * @param dependsOn 需要依赖的Bean的列表
     */
    fun setDependsOn(dependsOn: Array<String>)

    /**
     * 获取创建一个Bean时需要依赖的BeanDefinition的列表
     *
     * @return 需要依赖的BeanDefinition的beanName列表
     */
    fun getDependsOn(): Array<String>

}