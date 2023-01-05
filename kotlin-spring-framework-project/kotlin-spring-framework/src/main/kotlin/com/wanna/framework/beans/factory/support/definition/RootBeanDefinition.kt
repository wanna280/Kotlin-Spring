package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.core.type.MethodMetadata
import com.wanna.framework.lang.Nullable
import java.lang.reflect.Executable
import java.lang.reflect.Method

/**
 * 这是一个BeanDefinition的实现，是Spring的BeanFactory当中，完成了BeanDefinition的Merged之后的一个BeanDefinition，
 * 它也可以被用作BeanDefinition的注册，但是更加推荐使用GenericBeanDefinition，因为它支持动态配置parent BeanDefinition的方式；
 * 一个RootBeanDefinition，可能不止来自于一个BeanDefinition，可能是来自于多个它继承(通过parent)的BeanDefinition的合并
 *
 * @see BeanDefinition
 * @see GenericBeanDefinition
 */
open class RootBeanDefinition() : AbstractBeanDefinition() {
    constructor(@Nullable beanClass: Class<*>?) : this() {
        this.setBeanClass(beanClass)
    }

    /**
     * 调用super.copy方法将元素拷贝到当前对象当中
     *
     * @param beanDefinition 要去进行拷贝的BeanDefinition
     */
    constructor(beanDefinition: BeanDefinition) : this() {
        super.copy(beanDefinition, this)
        if (beanDefinition is RootBeanDefinition) {
            this.isFactoryBean = beanDefinition.isFactoryBean
            this.factoryMethodToIntrospect = beanDefinition.factoryMethodToIntrospect
            this.resolvedConstructorArguments = beanDefinition.resolvedConstructorArguments
            this.constructorArgumentsResolved = beanDefinition.constructorArgumentsResolved

        }
    }

    /**
     * factoryMethod(@Bean方法标注的方法)
     */
    @Nullable
    private var factoryMethodToIntrospect: Method? = null

    /**
     * 进行后置处理的锁
     */
    val postProcessLock = Any()

    /**
     * 是否已经被merged?
     */
    var postProcessed: Boolean = false

    /**
     * 当前的BeanDefinition，是否已经陈旧了?  需要去进行re-merge?
     */
    var stale: Boolean = false

    /**
     * 操作构造器的锁
     */
    val constructorArgumentLock = Any()

    /**
     * 已经解析的FactoryMethod或者Constructor
     */
    @Nullable
    var resolvedConstructorOrFactoryMethod: Executable? = null

    /**
     * 准备的构造器参数列表，需要完成解析后，才能成为最终的参数列表
     */
    @Nullable
    var preparedConstructorArguments: Array<out Any?>? = null

    /**
     * 已经解析出来的构造器参数列表
     */
    @Nullable
    var resolvedConstructorArguments: Array<out Any?>? = null

    /**
     * 构造器参数是否已经完成了解析? 如果已经完成了解析，那么就可以通过RootBeanDefinition当中去获取到构造器参数列表
     */
    var constructorArgumentsResolved = false

    /**
     * 一个BeanDefinition所装饰的BeanDefinition，因为需要维护beanName，所以这里保存一个BeanDefinitionHolder
     */
    @Nullable
    var decoratedDefinition: BeanDefinitionHolder? = null

    /**
     * 能否在beforeInstantiation当中解析到合适的Bean? 如果解析不到，那么下次就不会回调beforeInstantiation了(提高性能)
     */
    var beforeInstantiationResolved: Boolean = true

    /**
     * 是否已经解析出来destroy方法?
     */
    @Nullable
    var resolvedDestroyMethodName: String? = null

    /**
     * 是否是FactoryBean，可以为null代表还没解析过
     */
    @Nullable
    private var isFactoryBean: Boolean? = null

    open fun isFactoryBean(): Boolean? = isFactoryBean
    open fun setFactoryBean(@Nullable isFactoryBean: Boolean?) {
        this.isFactoryBean = isFactoryBean
    }

    /**
     * 检查给定的方法名和factoryMethodName是否匹配
     *
     * @param methodName methodName
     * @return 如果和factoryMethodName匹配的话, return true; 否则return false
     */
    open fun isFactoryMethod(methodName: String): Boolean {
        return this.getFactoryMethodName() == methodName
    }

    /**
     * 获取已经解析完成的FactoryMethod
     */
    @Nullable
    open fun getResolvedFactoryMethod(): Method? = this.factoryMethodToIntrospect

    /**
     * 设置已经解析完成的FactoryMethod
     */
    open fun setResolvedFactoryMethod(@Nullable method: Method?) {
        this.factoryMethodToIntrospect = method
    }

    /**
     * 获取FactoryMethod的Metadata信息
     *
     * @return Method Metadata
     */
    @Nullable
    open fun getFactoryMethodMetadata(): MethodMetadata? = null

}