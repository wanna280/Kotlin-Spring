package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.TypeConverter
import com.wanna.framework.beans.factory.HierarchicalBeanFactory
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.util.StringValueResolver
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.metrics.ApplicationStartup

/**
 * 这是一个支持配置的BeanFactory，它实现了SingletonBeanRegistry和HierarchicalBeanFactory，
 * 支持BeanFactory的继承(获取parentBeanFactory)功能，也支持了单实例Bean的注册中心功能
 *
 * 不仅如此，在当前接口当中，还新增了类型转换、Scope的注册，嵌入式值解析器的注册等功能。
 */
interface ConfigurableBeanFactory : HierarchicalBeanFactory, SingletonBeanRegistry {

    companion object {
        const val SCOPE_SINGLETON = "single"
        const val SCOPE_PROTOTYPE = "prototype"
    }

    /**
     * 类型转换器，用在Spring当中完成类型的转换，会组合ConversionService和PropertyEditor，协助去完成类型的转换
     */
    fun getTypeConverter(): TypeConverter
    fun setTypeConverter(typeConverter: TypeConverter)

    /**
     * 对外提供获取ConversionService的接口，去提供类型的转换的支持
     */
    fun getConversionService(): ConversionService?
    fun setConversionService(conversionService: ConversionService?)

    /**
     * 添加字符串的值解析器，比如用来解析"${}"占位符表达式
     *
     * @param resolver 你想添加的嵌入式值解析器
     */
    fun addEmbeddedValueResolver(resolver: StringValueResolver)

    /**
     * 当前BeanFactory当中是否有嵌入式的值解析器，如果有的话，才支持去进行表达式的解析
     *
     * @return 如果当前BeanFactory当中有嵌入式的值解析器的话，return true；不然return false
     */
    fun hasEmbeddedValueResolver(): Boolean

    /**
     * 使用嵌入式值解析器去解析目标表达式
     *
     * @param strVal 想要去进行解析的表达式(可以为null)
     * @return 解析完成的嵌入式值解析器
     */
    fun resolveEmbeddedValue(strVal: String?): String?

    /**
     * 设置当前BeanFactory要使用的BeanClassLoader
     *
     * @param classLoader 你想要去进行设置的BeanClassLoader，如果为空，将会使用默认的ClassLoader
     */
    fun setBeanClassLoader(classLoader: ClassLoader?)

    /**
     * 获取当前BeanFactory的BeanClassLoader
     */
    fun getBeanClassLoader(): ClassLoader

    /**
     * 获取已经注册的ScopeName列表
     *
     * @return 已经注册的Scope的scopeName列表
     */
    fun getRegisteredScopeNames(): Array<String>

    /**
     * 根据scopeName去获取已经注册的Scope
     *
     * @param name scopeName
     * @return Scope(如果没有注册的话，return null)
     */
    fun getRegisteredScope(name: String): Scope?

    /**
     * 注册指定Scope到BeanFactory当中
     *
     * @param name scopeName
     * @param scope 你想要注册的Scope
     */
    fun registerScope(name: String, scope: Scope)

    /**
     * 获取合并之后的BeanDefinition(支持从parentBeanFactory当中去进行获取)，提供公开的对外访问的接口
     *
     * @param name 想要去获取MergedBeanDefinition的beanName
     * @return 合并之后的BeanDefinition(一般为RootBeanDefinition)
     */
    fun getMergedBeanDefinition(name: String): BeanDefinition

    /**
     * BeanFactory也得提供获取ApplicationStartup的功能，提供getter/setter
     */
    fun setApplicationStartup(applicationStartup: ApplicationStartup)
    fun getApplicationStartup(): ApplicationStartup

}