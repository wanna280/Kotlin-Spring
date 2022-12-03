package com.wanna.boot.context.properties.bind

import com.wanna.framework.beans.factory.config.PlaceholderConfigurerSupport
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.PropertyPlaceholderHelper

/**
 * 基于[Environment]当中的[PropertySource]列表去提供占位符解析的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
open class PropertySourcesPlaceholdersResolver(environment: Environment, _helper: PropertyPlaceholderHelper? = null) :
    PlaceholdersResolver {

    /**
     * Environment内部的[PropertySource]列表
     */
    private val sources: Iterable<PropertySource<*>> =
        if (environment is ConfigurableEnvironment) environment.getPropertySources()
        else throw IllegalStateException("只有ConfigurableEnvironment才支持去获取PropertySources")

    /**
     * 提供占位符解析的Helper工具类
     */
    private val helper = _helper ?: PropertyPlaceholderHelper(
        PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_PREFIX,
        PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_SUFFIX,
        PlaceholderConfigurerSupport.DEFAULT_VALUE_SEPARATOR
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
}