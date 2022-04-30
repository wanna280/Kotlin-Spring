package com.wanna.framework.context

import com.wanna.framework.beans.factory.config.Scope
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.core.convert.ConversionService

interface ConfigurableBeanFactory : BeanFactory, SingletonBeanRegistry {

    /**
     * 获取BeanFactory的ClassLoader
     */
    fun getBeanClassLoader(): ClassLoader

    /**
     * 类型转换器，用在Spring当中完成类型的转换
     */
    fun getTypeConverter(): TypeConverter
    fun setTypeConverter(typeConverter: TypeConverter)

    /**
     * 获取ConversionService去完成类型的转换
     */
    fun getConversionService(): ConversionService?
    fun setConversionService(conversionService: ConversionService?)

    /**
     * 添加字符串的值解析器，比如用来解析"${}"占位符表达式
     */
    fun addEmbeddedValueResolver(resolver: StringValueResolver)

    /**
     * 是否有嵌入式的值解析器
     */
    fun hasEmbeddedValueResolver(): Boolean

    /**
     * 解析表达式
     */
    fun resolveEmbeddedValue(strVal: String?): String?

    /**
     * 设置BeanClassLoader
     * @param classLoader 要设置的ClassLoader，如果为空，将会使用默认的ClassLoader
     */
    fun setBeanClassLoader(classLoader: ClassLoader?)

    /**
     * 获取已经注册的ScopeName列表
     */
    fun getRegisteredScopeNames(): Array<String>

    /**
     * 根据scopeName去获取已经注册的Scope，如果该scopeName没有被注册，那么return null
     */
    fun getRegisteredScope(name: String): Scope?

    /**
     * 注册scope到BeanFactory当中
     */
    fun registerScope(name: String, scope: Scope)

    /**
     * 获取合并之后的BeanDefinition
     */
    fun getMergedBeanDefinition(beanName: String): BeanDefinition

}