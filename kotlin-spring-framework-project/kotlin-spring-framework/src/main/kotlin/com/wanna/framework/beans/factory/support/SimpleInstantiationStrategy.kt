package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.exception.BeanCreationException
import com.wanna.framework.core.NamedThreadLocal
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ReflectionUtils
import java.lang.reflect.Constructor
import java.lang.reflect.Method

/**
 * 这是一个简单的实例化策略，提供简单的实例化策略，支持根据构造器去进行对象的创建
 *
 * 支持使用下面三种方式对Bean去完成实例化
 *
 * * 1.给定BeanDefinition，从BeanDefinition当中去进行推断BeanClass和构造器
 * * 2.给定目标构造器和该构造器的参数列表
 * * 3.给定目标FactoryMethod和该方法的参数列表
 */
open class SimpleInstantiationStrategy : InstantiationStrategy {

    companion object {
        // 当前正在执行中的方法，类似一个栈结构方式的设计
        private val currentlyInvokeFactoryMethod: ThreadLocal<Method> = NamedThreadLocal("current Invoke FactoryMethod")

        /**
         * 获取当前正在执行的FactoryMethod(@Bean方法)，提供给外部去进行使用
         *
         * @return 当前正在执行的FactoryMethod(如果没有的话，return null)
         */
        @JvmStatic
        fun getCurrentlyInvokeFactoryMethod(): Method? {
            return currentlyInvokeFactoryMethod.get()
        }
    }

    /**
     * 给定一个MergedBeanDefinition，去创建Bean；
     *
     * * 1.如果MergedBeanDefinition当中已经解析出来了构造器，那么使用该构造器；
     * * 2.如果没有解析出来合适的构造器，那么从BeanClass当中去获取到无参数构造器去进行创建对象；
     *
     * * 3.如果该BeanDefinition有MethodOverride，那么需要使用CGLIB去创建代理对象
     *
     * @return 实例化完成的BeanInstance
     * @throws BeanCreationException 如果使用构造器实例化过程当中出现异常(比如类型是接口、比如没有解析到构造器)
     */
    override fun instantiate(bd: RootBeanDefinition, beanName: String?, owner: BeanFactory): Any {
        // 如果没有运行时方法重写，那么直接使用构造器去进行实例化
        if (!bd.hasMethodOverrides()) {
            var ctor = bd.resolvedConstructorOrFactoryMethod as Constructor<*>?  // 尝试从缓存当中去进行获取
            try {
                if (ctor == null) {
                    val beanClass = bd.getBeanClass() ?: throw IllegalStateException("无法解析出来beanClass，无法使用构造器完成实例化工作")
                    if (beanClass.isInterface) {
                        throw BeanCreationException("[beanName=$beanName]的beanClass不是一个具体的类，接口不能完成实例化")
                    }
                    ctor = beanClass.getDeclaredConstructor()
                    bd.resolvedConstructorOrFactoryMethod = ctor  // 设置已经解析的构造器为当前构造器，去进行缓存
                }
                if (ctor != null) {
                    return BeanUtils.instantiateClass(ctor)
                } else {
                    throw IllegalStateException("无法解析到beanClass的构造器！")
                }
            } catch (throwable: Throwable) {
                throw BeanCreationException(
                    "没有在[beanName=$beanName, beanClass=${bd.getBeanClass()}]当中找到默认的构造器",
                    throwable
                )
            }
        }
        // 如果有运行时方法重写，那么需要使用CGLIB去进行生成
        return instantiateWithMethodInjection(bd, beanName, owner)
    }

    /**
     * 使用MethodInjection的方式去进行实例化，也就是CGLIB的方式去完成实例化，交给子类去进行实现
     *
     * @param bd MergedBeanDefinition
     * @param beanName beanName
     * @param owner owner
     * @throws UnsupportedOperationException 如果使用的不是它的子类，但是使用了CGLIB的方式的话
     */
    protected open fun instantiateWithMethodInjection(
        bd: RootBeanDefinition, beanName: String?, owner: BeanFactory
    ): Any = throw java.lang.UnsupportedOperationException("不支持使用这种方式去对[beanName=$beanName]进行实例化")

    /**
     * 使用指定的构造器去完成实例化，并且参数列表已经给出了
     *
     * @param bd MergedBeanDefinition
     * @param beanName beanName
     * @param owner beanFactory
     * @param ctor 要使用的构造器
     * @param args 要使用的构造器的目标参数列表
     */
    override fun instantiate(
        bd: RootBeanDefinition, beanName: String?, owner: BeanFactory, ctor: Constructor<*>, vararg args: Any?
    ): Any {
        try {
            // 如果没有运行时的方法重写，那么直接使用构造器去进行实例化
            // 如果有运行时方法重写，那么需要使用CGLIB去进行生成
            return if (!bd.hasMethodOverrides()) {
                ReflectionUtils.makeAccessible(ctor)
                BeanUtils.instantiateClass(ctor, *args)
            } else {
                instantiateWithMethodInjection(bd, beanName, owner, ctor, args)
            }
        } catch (throwable: Throwable) {
            throw BeanCreationException(
                "创建Bean[beanName=$beanName, constructor=$ctor]失败", throwable
            )
        }
    }

    protected open fun instantiateWithMethodInjection(
        bd: RootBeanDefinition, beanName: String?, owner: BeanFactory, ctor: Constructor<*>?, vararg args: Any?
    ): Any = throw java.lang.UnsupportedOperationException("不支持使用这种方式去对[beanName=$beanName]进行实例化")

    /**
     * 给定FactoryMethod，去完成实例化
     *
     * @param bd MergedBeanDefinition
     * @param beanName beanName
     * @param owner beanFactory
     * @param factoryBean FactoryMethod所在的Bean
     * @param factoryMethod FactoryMethod(@Bean方法)
     * @param args 目标FactoryMethod的参数列表
     */
    override fun instantiate(
        bd: RootBeanDefinition,
        beanName: String?,
        owner: BeanFactory,
        factoryMethod: Method,
        factoryBean: Any,
        vararg args: Any?
    ): Any? {
        try {
            ReflectionUtils.makeAccessible(factoryMethod)  // make accessible
            // 记录之前的FactoryMethod(方便后续finally代码块当中去进行恢复)，设置为当前的FactoryMethod
            val beforeFactoryMethod = currentlyInvokeFactoryMethod.get()
            try {
                currentlyInvokeFactoryMethod.set(factoryMethod)
                // 根据参数，去执行目标FactoryMethod(@Bean)，反射执行目标方法
                val result: Any? = ReflectionUtils.invokeMethod(factoryMethod, factoryBean, *args)
                // 如果执行FactoryMethod返回了null，那么return NullBean；不然return result(正常创建)
                return result ?: NullBean()
            } finally {
                // 如果保存了before，那么恢复；如果没有保存before，那么需要remove
                if (beforeFactoryMethod != null) {
                    currentlyInvokeFactoryMethod.set(beforeFactoryMethod)
                } else {
                    currentlyInvokeFactoryMethod.remove()
                }
            }
        } catch (throwable: Throwable) {
            throw BeanCreationException("创建Bean[beanName=$beanName, factoryMethod=$factoryMethod]失败", throwable)
        }
    }
}