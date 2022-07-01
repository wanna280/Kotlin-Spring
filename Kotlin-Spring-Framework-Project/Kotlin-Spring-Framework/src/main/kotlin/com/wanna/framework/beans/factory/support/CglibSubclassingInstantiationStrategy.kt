package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.method.LookupOverride
import com.wanna.framework.beans.method.ReplaceOverride
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.ConfigurableBeanFactory
import com.wanna.framework.core.util.BeanUtils
import net.sf.cglib.proxy.CallbackFilter
import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.Factory
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import net.sf.cglib.proxy.NoOp
import java.lang.reflect.Constructor
import java.lang.reflect.Method

/**
 * 基于Cglib的子类生成策略，主要针对于某些需要进行某些运行时的Bean，使用CGLIB去生成子类并去完成实例化，
 * 主要针对于MethodOverride和ReplaceOverride这两种情况去进行处理
 */
open class CglibSubclassingInstantiationStrategy : SimpleInstantiationStrategy() {

    companion object {
        const val PASSTHROUGH = 0  // pass掉的数组索引
        const val LOOKUP_METHOD = 1  // 需要进行lookup的MethodInterceptor的数组索引
        const val METHOD_REPLACER = 2  // 需要进行replace的MethodInterceptor的数组索引
    }

    override fun instantiateWithMethodInjection(bd: RootBeanDefinition, beanName: String?, owner: BeanFactory): Any {
        return instantiateWithMethodInjection(bd, beanName, owner, null)
    }

    override fun instantiateWithMethodInjection(
        bd: RootBeanDefinition, beanName: String?, owner: BeanFactory, ctor: Constructor<*>?, vararg args: Any?
    ): Any {
        // 创建一个Cglib的子类生成器，去提供子类生成的支持
        val cglibSubClassCreator = CglibSubClassCreator(bd, owner)
        return cglibSubClassCreator.instantiate(ctor, *args)
    }

    /**
     * 这是一个Cglib的子类创建器，完成对BeanClass的子类的创建
     */
    private class CglibSubClassCreator(private val beanDefinition: RootBeanDefinition, private val owner: BeanFactory) {

        companion object {
            val CALLBACK_TYPES = arrayOf(
                NoOp::class.java,
                LookupOverrideMethodInterceptor::class.java,
                ReplaceOverrideMethodInterceptor::class.java
            )
        }

        private fun setCallbacks(instance: Any): Any {
            val factory = instance as Factory
            factory.callbacks = arrayOf(
                NoOp.INSTANCE,
                LookupOverrideMethodInterceptor(beanDefinition, owner),
                ReplaceOverrideMethodInterceptor(beanDefinition, owner)
            )
            return factory
        }

        /**
         * 使用构造器去实例化一个Cglib生成的子类对象
         *
         * @param ctor 要使用的构造器
         * @param args 构造器的参数列表
         */
        fun instantiate(ctor: Constructor<*>?, vararg args: Any?): Any {
            // 使用CGLIB生成子类
            val subClass = createEnhancedSubClass(beanDefinition)

            // 如果没有给定构造器的话，那么将会根据clazz去使用无参数构造器去创建对象
            val instance = if (ctor == null) {
                BeanUtils.instantiateClass(subClass)
            } else {
                // 从子类当中去找到相同的构造器参数的构造器，并进行实例化
                subClass.getDeclaredConstructor(*ctor.parameterTypes).newInstance(*args)
            }
            // 之前创建子类时，没有设置callback(设置的callbackTypes)，这里需要设置callback给实例
            return setCallbacks(instance)
        }

        /**
         * 使用CGLIB的Enhancer去创建一个被增强的子类
         *
         * @return 被CGLIB增强之后的子类
         */
        private fun createEnhancedSubClass(beanDefinition: RootBeanDefinition): Class<*> {
            val enhancer = Enhancer()
            enhancer.setSuperclass(beanDefinition.getBeanClass())  // set superClass
            if (owner is ConfigurableBeanFactory) {  // setClassLoader
                enhancer.classLoader = owner.getBeanClassLoader()
            }

            // setCallbackTypes and CallbackFilter
            enhancer.setCallbackTypes(CALLBACK_TYPES)
            enhancer.setCallbackFilter(MethodOverrideCallbackFilter(beanDefinition))
            return enhancer.createClass()
        }
    }

    private class MethodOverrideCallbackFilter(private val beanDefinition: RootBeanDefinition) : CallbackFilter {
        override fun accept(method: Method): Int {
            val methodOverride = beanDefinition.getMethodOverrides().getMethodOverride(method) ?: return PASSTHROUGH
            if (methodOverride is LookupOverride) {
                return LOOKUP_METHOD
            }
            if (methodOverride is ReplaceOverride) {
                return METHOD_REPLACER
            }
            throw java.lang.UnsupportedOperationException("不支持处理这样的方法")
        }
    }

    /**
     * 这是一个针对于@Lookup的运行时方法重写的CGLIB拦截器
     */
    private class LookupOverrideMethodInterceptor(
        private val beanDefinition: RootBeanDefinition, private val owner: BeanFactory
    ) : MethodInterceptor {
        override fun intercept(obj: Any?, method: Method, args: Array<out Any>?, proxy: MethodProxy?): Any {
            val lookupOverride = beanDefinition.getMethodOverrides().getMethodOverride(method) as LookupOverride
            val beanName = lookupOverride.beanName
            if (beanName.isNotBlank()) {
                return owner.getBean(beanName, method.returnType)
            }
            return owner.getBean(method.returnType)!!
        }
    }

    /**
     * 这是一个针对于Replacer的运行时方法重写的CGLIB，按照你指定的要进行重写的方式，去替代现有的方法
     */
    private class ReplaceOverrideMethodInterceptor(
        private val beanDefinition: RootBeanDefinition, private val owner: BeanFactory
    ) : MethodInterceptor {
        override fun intercept(obj: Any?, method: Method, args: Array<out Any>?, proxy: MethodProxy?): Any {
            val replaceOverride = beanDefinition.getMethodOverrides().getMethodOverride(method) as ReplaceOverride
            val methodReplacer = owner.getBean(replaceOverride.replacerBeanName, MethodReplacer::class.java)
            return methodReplacer.reimplement(obj, method, args)
        }
    }
}