package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.exception.BeanCreationException
import com.wanna.framework.core.util.BeanUtils
import com.wanna.framework.core.util.ReflectionUtils
import java.lang.reflect.Constructor
import java.lang.reflect.Method

/**
 * 这是一个简单的实例化策略，提供简单的实例化策略，支持根据构造器去进行对象的创建
 */
open class SimpleInstantiationStrategy : InstantiationStrategy {

    companion object {
        // 当前正在执行中的方法，类似一个栈结构方式的设计
        private val currentlyInvokeFactoryMethod: ThreadLocal<Method> = ThreadLocal()

        /**
         * 获取当前正在执行的FactoryMethod(@Bean方法)
         */
        @JvmStatic
        fun getCurrentlyInvokeFactoryMethod(): Method? {
            return currentlyInvokeFactoryMethod.get()
        }
    }

    override fun instantiate(bd: RootBeanDefinition, beanName: String?, owner: BeanFactory): Any {
        // 如果没有运行时方法重写，那么直接使用构造器去进行实例化
        if (!bd.hasMethodOverrides()) {
            var ctor = bd.resolvedConstructorOrFactoryMethod as Constructor<*>?

            try {
                if (ctor == null) {
                    val beanClass = bd.getBeanClass()
                    if (beanClass != null && beanClass.isInterface) {
                        throw BeanCreationException("BeanClass是一个接口，不能完成实例化")
                    }
                    ctor = beanClass!!.getDeclaredConstructor()
                    bd.resolvedConstructorOrFactoryMethod = ctor  // 设置已经解析的构造器为当前构造器
                }
                return BeanUtils.instantiateClass(ctor!!)
            } catch (throwable: Throwable) {
                throw BeanCreationException("没有在[beanName=$beanName, beanClass=${bd.getBeanClass()}]当中找到默认的构造器，[ex=$throwable]")
            }
        }
        // 如果有运行时方法重写，那么需要使用CGLIB去进行生成
        return instantiateWithMethodInjection(bd, beanName, owner)
    }

    protected open fun instantiateWithMethodInjection(
        bd: RootBeanDefinition, beanName: String?, owner: BeanFactory
    ): Any = throw java.lang.UnsupportedOperationException("不支持使用这种方式去对[beanName=$beanName]进行实例化")

    override fun instantiate(
        bd: RootBeanDefinition, beanName: String?, owner: BeanFactory, ctor: Constructor<*>, vararg args: Any?
    ): Any {
        try {
            // 如果没有运行时的方法重写，那么直接使用构造器去进行实例化
            // 如果有运行时方法重写，那么需要使用CGLIB去进行生成
            return if (!bd.hasMethodOverrides()) {
                ReflectionUtils.makeAccessiable(ctor)
                if (args.isEmpty()) BeanUtils.instantiateClass(ctor) else BeanUtils.instantiateClass(ctor, *args)
            } else {
                instantiateWithMethodInjection(bd, beanName, owner, ctor, args)
            }
        } catch (throwable: Throwable) {
            throw BeanCreationException("创建Bean[beanNmae=$beanName, constrcutor=$ctor]失败，原因是[throwable=$throwable]")
        }
    }

    protected open fun instantiateWithMethodInjection(
        bd: RootBeanDefinition, beanName: String?, owner: BeanFactory, ctor: Constructor<*>?, vararg args: Any?
    ): Any = throw java.lang.UnsupportedOperationException("不支持使用这种方式去对[beanName=$beanName]进行实例化")

    override fun instantiate(
        bd: RootBeanDefinition,
        beanName: String?,
        owner: BeanFactory,
        factoryMethod: Method,
        factoryBean: Any,
        vararg args: Any?
    ): Any? {
        try {
            ReflectionUtils.makeAccessiable(factoryMethod)  // make access
            // 记录之前的FactoryMethod(方便后续去进行恢复)，设置为当前的FactoryMethod
            val beforeFactoryMethod = currentlyInvokeFactoryMethod.get()
            try {
                currentlyInvokeFactoryMethod.set(factoryMethod)
                // 根据参数，去执行有参数和无参数的FactoryMethod，反射执行目标方法
                val result: Any? = if (args.isEmpty()) {
                    ReflectionUtils.invokeMethod(factoryMethod, factoryBean)
                } else {
                    ReflectionUtils.invokeMethod(factoryMethod, factoryBean, *args)
                }
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
            throw BeanCreationException("创建Bean[beanName=$beanName, factoryMethod=$factoryMethod]失败，原因是[throwable=$throwable]")
        }
    }
}