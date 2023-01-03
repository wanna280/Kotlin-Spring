package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.BeanWrapper
import com.wanna.framework.beans.BeanWrapperImpl
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.InjectionPoint
import com.wanna.framework.beans.factory.config.ConstructorArgumentValues
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.NamedThreadLocal
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import java.beans.ConstructorProperties
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method

/**
 * 这是一个构造器的解析器，负责完成Bean的构造器的解析，并使用构造器去完成Bean的创建，并支持对构造器/方法的参数这两种方式去进行Autowire
 *
 * @see AbstractAutowireCapableBeanFactory.instantiateUsingFactoryMethod
 * @see ConstructorResolver.autowireConstructor  使用构造器去进行实例化和注入
 * @see ConstructorResolver.instantiateUsingFactoryMethod  使用FactoryMethod去进行实例化和注入
 */
open class ConstructorResolver(private val beanFactory: AbstractAutowireCapableBeanFactory) {

    companion object {
        /**
         * 空参数的标识符
         */
        @JvmStatic
        private val EMPTY_ARGS = emptyArray<Any>()

        /**
         * 当前正在注入的的InjectionPoint(字段/方法参数)
         */
        @JvmStatic
        private val currentInjectionPoint = NamedThreadLocal<InjectionPoint>("Current Injection Point")

        /**
         * 设置新的InjectionPoint，并返回之前的InjectionPoint
         *
         * @param injectionPoint newInjectionPoint
         * @return oldInjectionPoint
         */
        @Nullable
        @JvmStatic
        fun setCurrentInjectionPoint(@Nullable injectionPoint: InjectionPoint?): InjectionPoint? {
            val old = currentInjectionPoint.get()
            if (injectionPoint != null) {
                currentInjectionPoint.set(old)
            } else {
                currentInjectionPoint.remove()
            }
            return old
        }
    }


    /**
     * 使用Constructor去完成Bean的实例化和自动注入，需要从候选的构造器当中，选出合适的构造器；
     * 如果没有合适的构造器，那么需要自己解析出来合适的构造器
     *
     * @param beanName beanName
     * @param mbd MergedBeanDefinition
     * @param ctors 候选的构造器列表(为空时会自动推断)
     * @param args 构造器的参数列表(可以为空)
     * @return beanWrapper
     */
    open fun autowireConstructor(
        beanName: String,
        mbd: RootBeanDefinition,
        @Nullable ctors: Array<Constructor<*>>?,
        @Nullable args: Array<out Any?>?
    ): BeanWrapper {
        val beanWrapper = BeanWrapperImpl()

        var constructorToUse: Constructor<*>?
        var argsToUse: Array<out Any?>? = null
        var argsToResolve: Array<out Any?>? = null

        // 1.检查BeanDefinition当中是否有已经缓存的构造器列表？
        synchronized(mbd.constructorArgumentLock) {
            constructorToUse = mbd.resolvedConstructorOrFactoryMethod as Constructor<*>?

            // 如果之前已经缓存了构造器的话，并且还解析好了参数的话，直接去进行获取即可
            if (constructorToUse != null && mbd.constructorArgumentsResolved) {
                argsToUse = mbd.resolvedConstructorArguments

                // 如果没有已经缓存好的参数列表，那么需要判断是否有preparedConstructorArguments
                // 这种类型的参数，是需要进行后期处理的
                if (argsToUse == null) {
                    argsToResolve = mbd.preparedConstructorArguments
                }
            }
            // 如果要完成参数的解析，那么在这里去完成参数的解析...
            if (argsToResolve != null) {
                argsToUse = emptyArray()
            }
        }

        // 2.如果没有找到合适的构造器的话
        if (constructorToUse == null || args == null) {

            // 2.1 如果没有推断出来合适的构造器，那么从beanClass当中去获取到所有的DeclaredConstructor
            var candidates = ctors
            // 如果没有给定合适的构造器的话，那么获取declaredConstructor
            if (candidates == null) {
                candidates = mbd.getBeanClass()!!.declaredConstructors
            }
            // 如果只要一个候选的构造器的话，那么就使用它创建对象，并且加入到缓存当中
            if (candidates != null && candidates.size == 1 && candidates[0].parameterCount == 0) {
                beanWrapper.setWrappedInstance(instantiate(mbd, beanName, beanFactory, candidates[0]))
                mbd.resolvedConstructorOrFactoryMethod = candidates[0]
                mbd.resolvedConstructorArguments = EMPTY_ARGS
                mbd.constructorArgumentsResolved = true
                return beanWrapper
            }

            var maxParamCount = 0
            // 遍历所有的构造器，去进行匹配，选择出来最合适的一个构造器，并完成对象的创建
            candidates?.forEach {
                val parameterCount = it.parameterCount
                if (maxParamCount < parameterCount) {
                    maxParamCount = parameterCount
                    constructorToUse = it
                }
            }
            if (constructorToUse != null) {
                // 从beanFactory当中去获取参数名的发现器，去提供参数名的发现的支持
                val parameterNameDiscoverer = this.beanFactory.getParameterNameDiscoverer()

                // 如果必要的话，从JDK当中提供的@ConstructorProperties注解当中去寻找构造器参数名...
                // 如果没有@ConstructorProperties注解的话，需要使用参数名发现器去进行构造器的参数名的获取...
                val cp = constructorToUse!!.getAnnotation(ConstructorProperties::class.java)
                val parameterNames: Array<String>? =
                    cp?.value ?: parameterNameDiscoverer?.getParameterNames(constructorToUse!!)


                // 在解析完所有的构造器参数名之后，需要去为该构造器去创建参数列表
                argsToUse = createArgumentArray(
                    beanName,
                    mbd,
                    beanWrapper,
                    null,
                    constructorToUse!!.parameterTypes,
                    parameterNames,
                    constructorToUse!!
                )
            }
        }

        // 使用构造器去进行实例化，并将beanInstance设置到BeanWrapper当中
        beanWrapper.setWrappedInstance(instantiate(mbd, beanName, beanFactory, constructorToUse!!, *argsToUse!!))
        return beanWrapper
    }

    /**
     * 解析出来合适的构造器以及构造器参数之后，就可以使用指定的构造器去完成Bean的实例化
     *
     * @param bd beanDefinition
     * @param beanName beanName(可以为null)
     * @param owner beanFactory
     * @param ctor 要进行实例化使用的构造器
     * @param args 构造器的方法参数列表
     * @return 创建好的beanInstance
     */
    private fun instantiate(
        bd: RootBeanDefinition, beanName: String?, owner: BeanFactory, ctor: Constructor<*>, vararg args: Any?
    ): Any {
        return beanFactory.getInstantiationStrategy().instantiate(bd, beanName, owner, ctor, *args)
    }

    /**
     * 使用FactoryMethod去执行目标方法完成实例化，并完成方法的自动注入
     *
     * @see RootBeanDefinition.factoryMethodToIntrospect
     */
    open fun instantiateUsingFactoryMethod(beanName: String, mbd: RootBeanDefinition): BeanWrapper {
        val beanWrapper = BeanWrapperImpl()
        beanFactory.initBeanWrapper(beanWrapper)

        val factoryMethodName = mbd.getFactoryMethodName()
        val factoryBeanName = mbd.getFactoryBeanName()
        var resolvedFactoryMethod: Method? = mbd.getResolvedFactoryMethod()

        // factoryBean and factoryClass
        var factoryBean: Any? = null
        var factoryClass: Class<*>? = null

        if (factoryMethodName != null) {
            factoryBean = beanFactory.getBean(factoryBeanName!!)
            factoryClass = ClassUtils.getUserClass(factoryBean)
        }


        // TODO, 这里有点问题, 不能这样搞, 目前先以可用为主...
        if (resolvedFactoryMethod == null) {
            ReflectionUtils.doWithMethods(factoryClass!!) {
                if (it.name == mbd.getFactoryMethodName()) {
                    if (resolvedFactoryMethod == null) {
                        resolvedFactoryMethod = it
                        mbd.setResolvedFactoryMethod(it)
                    }
                }
            }
        }

        val parameterNameDiscoverer = this.beanFactory.getParameterNameDiscoverer()
        val parameterNames = parameterNameDiscoverer?.getParameterNames(resolvedFactoryMethod!!)

        // 创建执行目标方法需要用到的参数数组
        val argumentArray = createArgumentArray(
            beanName,
            mbd,
            beanWrapper,
            mbd.getConstructorArgumentValues(),
            resolvedFactoryMethod!!.parameterTypes,
            parameterNames,
            resolvedFactoryMethod!!,
            true
        )

        // 使用BeanFactory提供的实例化策略，去完成实例化
        var instance = beanFactory.getInstantiationStrategy().instantiate(
            mbd, beanName, beanFactory, factoryMethod = resolvedFactoryMethod!!, factoryBean!!, *argumentArray
        )

        if (instance == null) {  // 如果从实例化的Supplier当中获取到了null，那么封装NullBean
            instance = NullBean()
        }

        // 后期设置beanInstance到beanWrapper当中
        beanWrapper.setWrappedInstance(instance)
        return beanWrapper
    }

    /**
     * 给指定的方法/构造器去创建参数数组，根据每个方法参数，去构建一个DependencyDescriptor交给beanFactory去进行依赖的解析
     *
     * @param beanName beanName
     * @param mbd MergedBeanDefinition
     * @param beanWrapper beanWrapper
     * @param resolveValues 构造器参数列表(可以为空)
     * @param paramTypes 参数类型列表
     * @param paramNames 参数名列表(可以为空)
     * @param executable 方法/构造器
     * @param autowiring 在构造器参数当中找不到合适的参数时，是否要进行自动注入？
     */
    private fun createArgumentArray(
        beanName: String,
        mbd: RootBeanDefinition,
        beanWrapper: BeanWrapper,
        resolveValues: ConstructorArgumentValues?,
        paramTypes: Array<Class<*>>,
        @Nullable paramNames: Array<String>?,
        executable: Executable,
        autowiring: Boolean = true
    ): Array<Any?> {
        // 创建一个参数数组(Array<Any?>)，去获取到方法需要的参数列表
        // 设置依赖描述符上的required=true，如果该方法参数上有"@Autowired(required=false)"时，在解析过程当中也支持
        val params: Array<Any?> = Array(paramTypes.size) {
            val methodParameter = MethodParameter(executable, it)
            beanFactory.resolveDependency(DependencyDescriptor(methodParameter, true, true), beanName)  // return
        }
        return params
    }
}