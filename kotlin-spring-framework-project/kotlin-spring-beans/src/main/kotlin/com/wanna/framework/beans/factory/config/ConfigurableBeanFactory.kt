package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.TypeConverter
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.HierarchicalBeanFactory
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.util.StringValueResolver
import com.wanna.framework.beans.factory.exception.NoSuchBeanDefinitionException
import com.wanna.framework.beans.factory.config.BeanPostProcessor
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.metrics.ApplicationStartup
import com.wanna.framework.lang.Nullable

/**
 * 这是一个支持配置的BeanFactory, 它实现了SingletonBeanRegistry和HierarchicalBeanFactory,
 * 支持BeanFactory的继承(获取parentBeanFactory)功能, 也支持了单实例Bean的注册中心功能
 *
 * 不仅如此, 在当前接口当中, 还新增了类型转换、Scope的注册, 嵌入式值解析器的注册等功能.
 *
 * @see HierarchicalBeanFactory
 * @see SingletonBeanRegistry
 */
interface ConfigurableBeanFactory : HierarchicalBeanFactory, SingletonBeanRegistry {

    companion object {
        /**
         * 单例Bean的Scope的常量
         */
        const val SCOPE_SINGLETON = "singleton"

        /**
         * 原型Bean的Scope的常量
         */
        const val SCOPE_PROTOTYPE = "prototype"
    }

    /**
     * 设置当前BeanFactory的parentBeanFactory
     *
     * @param parentBeanFactory Parent BeanFactory(可以为null)
     */
    fun setParentBeanFactory(@Nullable parentBeanFactory: BeanFactory?)

    /**
     * 获取当前BeanFactory当中的类型转换器(如果不存在的话, 将会采用默认的TypeConverter)
     *
     * @return TypeConverter(不会为null, 如果没设置的话, 会存在有默认的TypeConverter)
     */
    fun getTypeConverter(): TypeConverter

    /**
     * 自定义TypeConverter(用在Spring当中完成类型的转换, 会组合ConversionService和PropertyEditor, 协助去完成类型的转换)
     *
     * @param typeConverter 需要使用的TypeConverter
     */
    fun setTypeConverter(@Nullable typeConverter: TypeConverter?)

    /**
     * 对外提供获取ConversionService的接口, 去提供类型的转换的支持
     *
     * @return ConversionService(如果当前BeanFactory不存在的话, return null)
     */
    @Nullable
    fun getConversionService(): ConversionService?

    /**
     * 设置ConversionService
     *
     * @param conversionService ConversionService
     */
    fun setConversionService(@Nullable conversionService: ConversionService?)

    /**
     * 添加字符串的值解析器, 比如用来解析"${}"占位符表达式
     *
     * @param resolver 需要添加的嵌入式值解析器
     */
    fun addEmbeddedValueResolver(resolver: StringValueResolver)

    /**
     * 当前BeanFactory当中是否有嵌入式的值解析器, 如果有的话, 才支持去进行表达式的解析
     *
     * @return 如果当前BeanFactory当中有嵌入式的值解析器的话, return true; 否则的话return false
     */
    fun hasEmbeddedValueResolver(): Boolean

    /**
     * 使用嵌入式值解析器去解析目标表达式
     *
     * @param strVal 想要去进行解析的表达式(可以为null)
     * @return 解析完成的嵌入式值(可以为null)
     */
    @Nullable
    fun resolveEmbeddedValue(@Nullable strVal: String?): String?

    /**
     * 设置当前BeanFactory要使用的BeanClassLoader
     *
     * @param classLoader 你想要去进行设置的BeanClassLoader, 如果为空, 将会使用默认的ClassLoader
     */
    fun setBeanClassLoader(@Nullable classLoader: ClassLoader?)

    /**
     * 获取当前BeanFactory的BeanClassLoader
     *
     * @return BeanClassLoader
     */
    fun getBeanClassLoader(): ClassLoader

    /**
     * 获取已经注册的ScopeName列表
     *
     * @return 已经注册到当前BeanFactory当中的Scope的scopeName列表
     */
    fun getRegisteredScopeNames(): Array<String>

    /**
     * 根据scopeName去获取已经注册的Scope
     *
     * @param name scopeName
     * @return Scope(如果没有注册过该scopeName对应的Scope的话, return null)
     */
    @Nullable
    fun getRegisteredScope(name: String): Scope?

    /**
     * 注册指定Scope到BeanFactory当中
     *
     * @param name scopeName
     * @param scope 你想要注册的Scope
     */
    fun registerScope(name: String, scope: Scope)

    /**
     * 设置某个Bean是否正在创建当中的状态
     *
     * @param beanName beanName
     * @param inCreation inCreation
     */
    fun setCurrentlyInCreation(beanName: String, inCreation: Boolean)

    /**
     * 是否当前Bean正在创建当中
     *
     * @param beanName beanName
     */
    fun isCurrentlyInCreation(beanName: String): Boolean

    /**
     * 添加BeanPostProcessor
     *
     * @param processor 你想要往BeanFactory当中添加的BeanPostProcessor
     */
    fun addBeanPostProcessor(processor: BeanPostProcessor)

    /**
     * 根据type移除BeanPostProcessor
     *
     * @param type 要去移除的BeanPostProcessor的类型
     */
    fun removeBeanPostProcessor(type: Class<*>)

    /**
     * 根据index去移除BeanPostProcessor
     *
     * @param index 具体的index
     */
    fun removeBeanPostProcessor(index: Int)

    /**
     * 获取当前BeanFactory当中的BeanPostProcessor的数量
     *
     * @return Count of BeanPostProcessor
     */
    fun getBeanPostProcessorCount(): Int


    /**
     * 获取合并之后的BeanDefinition(支持从parentBeanFactory当中去进行获取), 提供公开的对外访问的接口
     *
     * @param name 想要去获取MergedBeanDefinition的beanName
     * @return 合并之后的BeanDefinition(一般为RootBeanDefinition)
     * @throws NoSuchBeanDefinitionException 如果BeanFactory当中没有这样的beanName的BeanDefinition
     */
    @Throws(NoSuchBeanDefinitionException::class)
    fun getMergedBeanDefinition(name: String): BeanDefinition

    /**
     * BeanFactory也得提供获取ApplicationStartup的功能, 提供getter/setter
     */
    /**
     * 设置[ApplicationStartup], 提供Spring BeanFactory启动过程当中的指标监控
     *
     * @param applicationStartup ApplicationStartup
     */
    fun setApplicationStartup(applicationStartup: ApplicationStartup)

    /**
     * 获取当前[ConfigurableBeanFactory]的[ApplicationStartup]
     *
     * @return ApplicationStartup
     */
    fun getApplicationStartup(): ApplicationStartup

    /**
     * 根据beanName去判断该Bean是否是一个FactoryBean
     *
     * @param name beanName of BeanDefinition
     * @return 该Bean是否是一个FactoryBean
     * @throws NoSuchBeanDefinitionException 如果BeanFactory当中不存在这样beanName的BeanDefinition的话
     */
    @Throws(NoSuchBeanDefinitionException::class)
    fun isFactoryBean(name: String): Boolean

    /**
     * 摧毁一个在特定的Scope内的Bean(Note: 任何的在摧毁过程当中的异常都应该被catch并记录日志, 而不是传播给调用方去进行处理)
     *
     * @param beanName beanName of BeanDefinition
     * @throws NoSuchBeanDefinitionException 如果BeanFactory当中没有这样的beanName的BeanDefinition
     */
    @Throws(NoSuchBeanDefinitionException::class)
    fun destroyScopedBean(beanName: String)

    /**
     * 摧毁一个Bean(Note: 任何的在摧毁过程当中的异常都应该被catch并记录日志, 而不是传播给调用方去进行处理)
     *
     * @param beanName beanName of BeanDefinition
     * @param bean 需要去进行摧毁的Bean
     * @throws NoSuchBeanDefinitionException 如果BeanFactory当中没有这样的beanName的BeanDefinition
     */
    @Throws(NoSuchBeanDefinitionException::class)
    fun destroyBean(beanName: String, bean: Any)

    /**
     * 摧毁当前BeanFactory当中的所有的单例Bean(Note: 任何的在摧毁过程当中的异常都应该被catch并记录日志, 而不是传播给调用方去进行处理);
     * 通常只有在BeanFactory关闭时, 才会需要去摧毁所有的单例Bean
     */
    fun destroySingletons()

}