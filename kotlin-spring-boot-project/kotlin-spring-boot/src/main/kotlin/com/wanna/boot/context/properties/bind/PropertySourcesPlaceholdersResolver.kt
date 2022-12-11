package com.wanna.boot.context.properties.bind

import com.wanna.framework.beans.factory.config.PlaceholderConfigurerSupport.Companion.DEFAULT_PLACEHOLDER_PREFIX
import com.wanna.framework.beans.factory.config.PlaceholderConfigurerSupport.Companion.DEFAULT_PLACEHOLDER_SUFFIX
import com.wanna.framework.beans.factory.config.PlaceholderConfigurerSupport.Companion.DEFAULT_VALUE_SEPARATOR
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.PropertyPlaceholderHelper

/**
 * 基于[PropertySource]的方式去提供占位符解析的[PropertyPlaceholderHelper]工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 *
 * @param _sources PropertySources
 * @param _helper 提供占位符的解析的Helper工具类
 */
open class PropertySourcesPlaceholdersResolver(
    _sources: Iterable<PropertySource<*>>,
    _helper: PropertyPlaceholderHelper? = null
) : PlaceholdersResolver {

    /**
     * 提供一个直接给定[PropertySource]列表的方式去构建[PropertySourcesPlaceholdersResolver]
     *
     * @param propertySources PropertySource列表
     */
    constructor(propertySources: Iterable<PropertySource<*>>) : this(propertySources, null)

    /**
     * 提供一个基于[Environment]去构建的[PropertySourcesPlaceholdersResolver]的方式
     *
     * @param environment Environment
     */
    constructor(environment: Environment) : this(getSources(environment))

    /**
     * 提供占位符解析时需要用到的属性信息的[PropertySource]列表
     */
    private val sources: Iterable<PropertySource<*>> = _sources

    /**
     * 提供占位符解析的Helper工具类
     */
    private val helper = _helper ?: PropertyPlaceholderHelper(
        DEFAULT_PLACEHOLDER_PREFIX,
        DEFAULT_PLACEHOLDER_SUFFIX,
        DEFAULT_VALUE_SEPARATOR
    )

    /**
     * 执行占位符的解析
     *
     * @param value 待解析的占位符
     * @return 解析完成占位符的结果
     */
    @Nullable
    override fun resolvePlaceholder(@Nullable value: Any?): Any? {
        if (value is String) {
            return helper.replacePlaceholder(value, this::resolvePlaceholder)
        }
        return value
    }

    /**
     * 利用所有的[PropertySource]去提供占位符的解析
     *
     * @param placeholder 待解析的占位符的属性值
     * @return 解析得到的占位符的属性值的结果(解析失败return null)
     */
    @Nullable
    protected open fun resolvePlaceholder(placeholder: String): String? {
        for (source in sources) {
            val property = source.getProperty(placeholder)
            if (property != null) {
                return property.toString()
            }
        }
        return null
    }

    companion object {

        /**
         * 从[Environment]当中去获取到合适的[PropertySource]列表
         *
         * @param environment Environment
         * @return PropertySource列表
         */
        @JvmStatic
        private fun getSources(environment: Environment): Iterable<PropertySource<*>> {
            return if (environment is ConfigurableEnvironment) environment.getPropertySources()
            else throw IllegalStateException("只有ConfigurableEnvironment才支持去获取PropertySources, 提供Placeholder的解析")
        }
    }
}