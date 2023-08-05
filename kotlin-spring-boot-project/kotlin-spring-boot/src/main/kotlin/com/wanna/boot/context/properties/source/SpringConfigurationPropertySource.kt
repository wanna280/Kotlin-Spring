package com.wanna.boot.context.properties.source

import com.wanna.boot.origin.PropertySourceOrigin
import com.wanna.framework.core.environment.EnumerablePropertySource
import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.environment.SystemEnvironmentPropertySource
import com.wanna.framework.lang.Nullable

/**
 * 将Spring原生的[PropertySource]去转换成为[ConfigurationPropertySource]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 *
 * @param propertySource 要去进行封装的[PropertySource], 提供对于属性获取到来源
 * @param propertyMappers PropertyMappers, 提供对于属性名的映射
 */
open class SpringConfigurationPropertySource(
    val propertySource: PropertySource<*>,
    val propertyMappers: Array<PropertyMapper>
) : ConfigurationPropertySource {

    init {
        // 对于PropertyMapper不能为空
        if (propertyMappers.isEmpty()) {
            throw IllegalStateException("PropertyMappers must contain at least one item")
        }
    }

    /**
     * 根据属性名去获取到属性值, Note: 这里name可能给定的是"user.name"这样的属性,
     * 但是在[PropertySource]当中, 可能并不存在有"user.name"这样的属性名, 但是存在有"USER_NAME"这样的属性名,
     * 我们可以认为它们俩是等价的属性, 因此我们可以将"user.name"去转换成为"USER_NAME", 这样就可以去[PropertySource]当中去进行获取了;
     *
     * @param name name
     * @return 根据属性名去获取到的属性值(获取不到return null)
     */
    @Nullable
    override fun getConfigurationProperty(@Nullable name: ConfigurationPropertyName?): ConfigurationProperty? {
        name ?: return null
        // 根据所有的PropertyMapper, 尝试去进行map, 得到所有的候选属性名, 从PropertySource当中去进行获取到对应的属性
        for (propertyMapper in propertyMappers) {
            for (propertyName in propertyMapper.map(name)) {
                val value = propertySource.getProperty(propertyName)
                if (value != null) {
                    val origin = PropertySourceOrigin.get(propertySource, propertyName)
                    return ConfigurationProperty.of(this, name, value, origin)
                }
            }
        }
        return null
    }

    /**
     * toString, 直接使用[PropertySource]去进行生成即可
     *
     * @return toString
     */
    override fun toString(): String = this.propertySource.toString()


    companion object {
        /**
         * 默认的[PropertyMapper]
         */
        @JvmStatic
        private val DEFAULT_MAPPERS: Array<PropertyMapper> = arrayOf(DefaultPropertyMapper.INSTANCE)

        /**
         * SystemEnvironment的[PropertySource], 需要同时使用[SystemEnvironmentPropertyMapper]和[DefaultPropertyMapper]
         */
        @JvmStatic
        private val SYSTEM_ENVIRONMENT_MAPPERS: Array<PropertyMapper> =
            arrayOf(SystemEnvironmentPropertyMapper.INSTANCE, DefaultPropertyMapper.INSTANCE)

        /**
         * 根据一个原生的Spring的[PropertySource]去构建出来[SpringConfigurationPropertySource];
         * 如果支持去进行迭代的话, 会创建一个[SpringIterableConfigurationPropertySource]
         *
         * @param propertySource PropertySource
         * @return ConfigurationPropertySource
         */
        @JvmStatic
        fun from(propertySource: PropertySource<*>): SpringConfigurationPropertySource {
            // 获取PropertyMapper
            val propertyMappers = getPropertyMappers(propertySource)
            // 如果它是一个可以去进行迭代的PropertySource, 那么创建一个支持去进行迭代的SpringIterableConfigurationPropertySource
            if (isFullEnumerable(propertySource) && propertySource is EnumerablePropertySource<*>) {
                return SpringIterableConfigurationPropertySource(propertySource, propertyMappers)
            }
            // 如果不是一个可以去进行迭代的PropertySource, 那么直接创建一个普通的SpringConfigurationPropertySource
            return SpringConfigurationPropertySource(propertySource, propertyMappers)
        }

        /**
         * 根据给定的[PropertySource], 去获取到对该类型的[PropertySource]去提供属性值映射功能的[PropertyMapper]
         *
         * @param source PropertySource
         * @return 为给PropertySource提供属性值映射的PropertyMappers
         */
        @JvmStatic
        private fun getPropertyMappers(source: PropertySource<*>): Array<PropertyMapper> {
            // 如果它是一个SystemEnvironmentPropertySource的话, 需要使用SystemEnvironmentPropertyMapper和DefaultPropertyMapper
            // 因为SystemEnvironment当中的属性名, 和正常的属性名不一样, 是含有大量的"_"符号的, 比如"USER_NAME"
            if (source is SystemEnvironmentPropertySource && hasSystemEnvironmentName(source)) {
                return SYSTEM_ENVIRONMENT_MAPPERS
            }
            // 如果是普通的PropertySource的话, 那么只需要使用DefaultPropertyMapper
            return DEFAULT_MAPPERS
        }

        /**
         * 检查给定的[PropertySource]的name是否是包含有[StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME]
         *
         * @param source source
         * @return 如果name和SystemEnvironment匹配的话, return true; 否则return false
         */
        private fun hasSystemEnvironmentName(source: PropertySource<*>): Boolean {
            return source.name == StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME
                    || source.name.endsWith("-" + StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)
        }

        /**
         * 判断给定的[PropertySource]的PropertyName是否是可以迭代的?
         *
         * @param propertySource PropertySource
         * @return 如果它是[EnumerablePropertySource]的话, 那么就是可以迭代的, 不然不允许去进行迭代
         */
        @JvmStatic
        private fun isFullEnumerable(propertySource: PropertySource<*>): Boolean {
            val rootPropertySource = getRootPropertySource(propertySource)
            if (rootPropertySource.source is Map<*, *>) {
                // Check we're not security restricted ???
                try {
                    (rootPropertySource.source as Map<*, *>).size
                } catch (ex: Throwable) {
                    return false
                }
            }
            return rootPropertySource is EnumerablePropertySource<*>
        }


        /**
         * 获取Root PropertySource, 因为存在有PropertySource的source还是PropertySource的情况(没见过)
         *
         * @param propertySource 原始的PropertySource
         * @return Root PropertySource
         */
        @JvmStatic
        private fun getRootPropertySource(propertySource: PropertySource<*>): PropertySource<*> {
            var source: PropertySource<*> = propertySource
            while (source.source != null && source.source is PropertySource<*>) {
                source = source.source as PropertySource<*>
            }
            return source
        }
    }
}