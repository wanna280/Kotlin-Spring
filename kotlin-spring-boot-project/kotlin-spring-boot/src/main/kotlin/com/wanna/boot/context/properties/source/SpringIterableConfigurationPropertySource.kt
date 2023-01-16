package com.wanna.boot.context.properties.source

import com.wanna.boot.origin.OriginLookup
import com.wanna.boot.origin.PropertySourceOrigin
import com.wanna.framework.core.environment.EnumerablePropertySource
import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.lang.Nullable
import java.util.*
import java.util.function.Supplier

/**
 * 针对Spring的[PropertySource]去提供[ConfigurationPropertyName]的迭代功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 *
 * @param propertySource 可以支持去进行属性名的迭代的[PropertySource]
 * @param propertyMappers PropertyMappers, 提供对于属性名的映射
 */
open class SpringIterableConfigurationPropertySource(
    propertySource: EnumerablePropertySource<*>,
    propertyMappers: Array<PropertyMapper>
) : SpringConfigurationPropertySource(propertySource, propertyMappers), IterableConfigurationPropertySource {

    /**
     * 如果当前的PropertySource是immutable的话, 那么可以将结果去进行缓存起来, 避免每次重新生成
     */
    @Volatile
    @Nullable
    private var configurationPropertyNames: Array<ConfigurationPropertyName>? = null

    /**
     * Mappings, 维护当前[PropertySource]当中的属性名的映射信息, propertyName与ConfigurationPropertyName之间的双向映射
     *
     * TODO, 这里应该可以改成Soft Reference的Cache
     */
    @Nullable
    private var mappings: Mappings? = null

    @Nullable
    override fun getConfigurationProperty(@Nullable name: ConfigurationPropertyName?): ConfigurationProperty? {
        name ?: return null

        // 1.如果通过父类, 就已经可以获取到对应的Property了, 那么return;
        // 这个主要是变换ConfigurationPropertyName->propertyName, 去PropertySource当中去进行获取
        val property = super.getConfigurationProperty(name)
        if (property != null) {
            return property
        }

        // 2.如果根据父类, 无法获取到的话, 那么尝试利用使用Mappings去进行匹配...
        // 这个主要是变化PropertySource当中的propertyName->ConfigurationPropertyName, 再和给定的ConfigurationPropertyName去进行匹配
        for (propertyName in getMappings().getMapped(name)) {
            val value = getPropertySource().getProperty(propertyName)
            if (value != null) {
                val origin = PropertySourceOrigin.get(propertySource, propertyName)
                return ConfigurationProperty.of(this, name, value, origin)
            }
        }
        return null
    }

    /**
     * 获取到用于去对[ConfigurationPropertyName]去进行迭代的迭代器
     *
     * @return [ConfigurationPropertyName]的迭代器(Kotlin可以直接对数组去使用迭代器, 因此直接返回)
     */
    override fun iterator(): Iterator<ConfigurationPropertyName> = getConfigurationPropertyNames().iterator()

    /**
     * 获取当前的[PropertySource]当中需要去进行迭代的属性名列表, 分别去构建出来一个[ConfigurationPropertyName]
     *
     * @return ConfigurationPropertyName List
     */
    private fun getConfigurationPropertyNames(): Array<ConfigurationPropertyName> {
        // 如果它是一个可变的PropertySource, 那么每次都返回一个新的ConfigurationPropertyName列表
        if (!isImmutablePropertySource()) {
            return getMappings().getConfigurationPropertyNames(getPropertySource().getPropertyNames()).filterNotNull()
                .toTypedArray()
        }
        // 如果它是一个不可变的ConfigurationPropertySource, 那么直接缓存一份...后续都直接使用缓存的数据即可
        var configurationPropertyNames = this.configurationPropertyNames
        if (configurationPropertyNames == null) {
            configurationPropertyNames =
                getMappings().getConfigurationPropertyNames(getPropertySource().getPropertyNames()).filterNotNull()
                    .toTypedArray()
            this.configurationPropertyNames = configurationPropertyNames
        }
        return configurationPropertyNames
    }

    /**
     * 快速将[PropertySource]去转换成为[EnumerablePropertySource], 因为类型一定是[EnumerablePropertySource]
     *
     * @return EnumerablePropertySource
     */
    open fun getPropertySource(): EnumerablePropertySource<*> = this.propertySource as EnumerablePropertySource<*>

    /**
     * 检查当前的[ConfigurationPropertySource]当中是否存在有给定的属性Key作为前缀的配置信息
     *
     * @param name 属性前缀Key
     * @return 如果存在return PRESENT, 如果不存在, return ABSENT; 默认实现为UNKNOWN
     */
    override fun containsDescendantOf(name: ConfigurationPropertyName): ConfigurationPropertyState {
        getConfigurationPropertyNames().forEach {
            if (name.isAncestorOf(it)) {
                return ConfigurationPropertyState.PRESENT
            }
        }
        return ConfigurationPropertyState.ABSENT
    }

    /**
     * 获取当前[PropertySource]的Mappings
     *
     * @return Mappings
     */
    private fun getMappings(): Mappings {
        var mappings = this.mappings
        if (mappings == null) {
            mappings = createMappings()
            updateMappings(mappings)
            this.mappings = mappings
        }
        return mappings
    }


    /**
     * 创建Mappings
     *
     * @return Mappings
     */
    private fun createMappings(): Mappings {
        return Mappings(propertyMappers, isImmutablePropertySource(), false)
    }

    /**
     * 更新Mappings
     *
     * @param mappings 要去进行更新的Mappings
     */
    private fun updateMappings(mappings: Mappings) {
        mappings.updateMappings(getPropertySource()::getPropertyNames)
    }

    /**
     * 检查当前的PropertySource是否是一个不可变的[PropertySource]
     *
     * @return 如果PropertySource不可变return true; 否则return false
     */
    private fun isImmutablePropertySource(): Boolean {
        val propertySource = propertySource
        if (propertySource is OriginLookup<*>) {
            return propertySource.isImmutable()
        }
        if (StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME == propertySource.name) {
            return propertySource.source == System.getenv()
        }
        return false
    }

    /**
     * Mappings, 维护一个PropertySource当中的属性名的映射信息, propertyName <-> ConfigurationPropertyName
     */
    private class Mappings(
        private val mappers: Array<PropertyMapper>,
        private val immutable: Boolean,
        private val trackDescendants: Boolean
    ) {

        /**
         * Key-ConfigurationPropertyName, Value-该ConfigurationPropertyName映射得到的属性名列表,
         * Note: ConfigurationPropertyName需要实现hashCode&equals方法
         */
        @Nullable
        @Volatile
        private var mappings: MutableMap<ConfigurationPropertyName, Set<String>>? = null

        /**
         * Key-propertyName, Value-根据propertyName去转换得到的ConfigurationPropertyName
         */
        @Nullable
        @Volatile
        private var reverseMappings: MutableMap<String, ConfigurationPropertyName>? = null

        /**
         * TODO
         */
        @Nullable
        @Volatile
        private var descendants: MutableMap<ConfigurationPropertyName, Set<ConfigurationPropertyName>>? = null

        /**
         * 上一次进行更新的属性名快照, 只有immutable时才会记录快照, 不是immutable的话, 为null
         */
        @Nullable
        @Volatile
        private var configurationPropertyNames: Array<ConfigurationPropertyName?>? = null

        /**
         * 上一次进行更新的属性名快照, 只有immutable时才会记录快照, 不是immutable的话, 为null
         */
        @Nullable
        @Volatile
        private var lastUpdated: Array<String>? = null

        /**
         * 为[ConfigurationPropertyName]去获取到映射得到的属性名列表
         *
         * @param configurationPropertyName ConfigurationPropertyName
         * @return 映射得到的属性名咩表
         */
        fun getMapped(configurationPropertyName: ConfigurationPropertyName): Set<String> {
            return mappings?.get(configurationPropertyName) ?: Collections.emptySet()
        }

        /**
         * 为给定的这些属性名, 去获取到对应的[ConfigurationPropertyName]列表
         *
         * @param propertyNames 当前PropertySource当中的PropertyNames列表
         * @return 为PropertyNames去获取到的[ConfigurationPropertyName]列表
         */
        fun getConfigurationPropertyNames(propertyNames: Array<String>): Array<ConfigurationPropertyName?> {
            // 如果可以获取到ConfigurationPropertyNames, 说明是immutable的, 直接返回之前缓存起来的快照即可
            val names = this.configurationPropertyNames
            if (names != null) {
                return names
            }

            // 如果无法获取到ConfigurationPropertyNames, 那么说明不是immutable的, 需要创建一份新对象去进行返回
            val reverseMappings = this.reverseMappings
            if (reverseMappings.isNullOrEmpty()) {
                return emptyArray()
            }
            return Array(propertyNames.size) { reverseMappings[propertyNames[it]] }
        }

        /**
         * 更新Mappings
         *
         * @param propertyNames 提供PropertyName的Supplier
         */
        fun updateMappings(propertyNames: Supplier<Array<String>>) {
            if (this.mappings == null || !this.immutable) {
                var count = 0
                while (true) {
                    try {
                        updateMappings(propertyNames.get())
                        return
                    } catch (ex: ConcurrentModificationException) {
                        if ((count++) > 10) {
                            throw ex
                        }
                    }
                }
            }
        }

        /**
         * 根据当前这一时刻的[PropertySource]当中的propertyName列表, 去进行更新Mappings
         *
         * @param propertyNames 当前这一时刻的PropertySource当中的PropertyNames列表
         */
        private fun updateMappings(propertyNames: Array<String>) {
            val lastUpdated = this.lastUpdated
            if (lastUpdated != null && Arrays.equals(propertyNames, lastUpdated)) {
                return
            }
            val size = propertyNames.size
            val mappings = cloneOrCreate(mappings, size)
            val reverseMappings = cloneOrCreate(this.reverseMappings, size)

            for (mapper in this.mappers) {
                for (propertyName in propertyNames) {
                    if (!reverseMappings.containsKey(propertyName)) {
                        val configurationPropertyName = mapper.map(propertyName)
                        add(mappings, configurationPropertyName, propertyName)
                        reverseMappings[propertyName] = configurationPropertyName
                    }
                }
            }

            this.mappings = mappings
            this.reverseMappings = reverseMappings

            // 如果是immutable的话, 那么将propertyNames缓存下来
            this.lastUpdated = if (immutable) propertyNames else null

            // 如果是immutable的话, 那么将ConfigurationPropertyNames去缓存一份...
            this.configurationPropertyNames = if (immutable) reverseMappings.values.toTypedArray() else null
        }

        private fun <K, T> add(map: MutableMap<K, Set<T>>, key: K, value: T) {
            val tSet = map.computeIfAbsent(key) { LinkedHashSet() }
            if (tSet is MutableSet<*>) {
                (tSet as MutableSet<T>).add(value)
            }
        }

        private fun <K, V> cloneOrCreate(@Nullable source: MutableMap<K, V>?, size: Int): MutableMap<K, V> {
            return if (source == null) LinkedHashMap(size) else LinkedHashMap(source)
        }
    }
}