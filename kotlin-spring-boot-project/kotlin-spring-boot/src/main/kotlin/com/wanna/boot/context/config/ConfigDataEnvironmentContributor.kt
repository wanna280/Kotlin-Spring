package com.wanna.boot.context.config

import com.wanna.boot.context.properties.source.ConfigurationPropertySource
import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.lang.Nullable
import java.util.Collections
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 */
open class ConfigDataEnvironmentContributor private constructor(val kind: Kind) :
    Iterable<ConfigDataEnvironmentContributor> {

    /**
     * 已经存在的PropertySource
     */
    @Nullable
    private var propertySource: PropertySource<*>? = null

    /**
     * 已经存在的PropertySource
     */
    @Nullable
    private var configurationPropertySource: ConfigurationPropertySource? = null

    @Nullable
    private var configDataProperties: ConfigDataProperties? = null

    @Nullable
    private var resource: ConfigDataResource? = null

    @Nullable
    private var location: ConfigDataLocation? = null

    /**
     * 是否指定了Profile?
     */
    private var profileSpecific: Boolean = false

    /**
     * children
     */
    private var children: Map<ImportPhase, List<ConfigDataEnvironmentContributor>> = mapOf()

    private constructor(children: Map<ImportPhase, List<ConfigDataEnvironmentContributor>>) : this(Kind.ROOT) {
        this.children = children
    }

    private constructor(propertySource: PropertySource<*>) : this(Kind.EXISTING) {
        this.propertySource = propertySource
        this.configurationPropertySource = ConfigurationPropertySource.from(propertySource)
    }

    private constructor(properties: ConfigDataProperties) : this(Kind.INITIAL) {
        this.configDataProperties = properties
    }

    /**
     * 检查当前的Contributor当中是否有ConfigurationPropertySource
     *
     * @return 如果有ConfigurationPropertySource, return true; 否则return false
     */
    open fun hasConfigurationPropertySource(): Boolean = this.configurationPropertySource != null

    /**
     * 获取PropertySource
     *
     * @return ConfigurationPropertySource
     */
    @Nullable
    open fun getConfigurationPropertySource(): ConfigurationPropertySource? = this.configurationPropertySource

    /**
     * 获取PropertySource
     *
     * @return PropertySource(or null)
     */
    @Nullable
    open fun getPropertySource(): PropertySource<*>? = this.propertySource

    /**
     * 获取ConfigDataLocation
     *
     * @return ConfigDataLocation
     */
    @Nullable
    open fun getLocation(): ConfigDataLocation? = this.location

    /**
     * 以Stream的方式去迭代ConfigDataEnvironmentContributor
     *
     * @return Stream<ConfigDataEnvironmentContributor>
     */
    open fun stream(): Stream<ConfigDataEnvironmentContributor> = StreamSupport.stream(spliterator(), false)

    override fun iterator(): Iterator<ConfigDataEnvironmentContributor> = ContributorIterator()

    open fun isActive(@Nullable activationContext: ConfigDataActivationContext?): Boolean {
        if (this.kind == Kind.UNBOUND_IMPORT) {
            return false
        }
        return this.configDataProperties?.isActive(activationContext) ?: true
    }

    /**
     * 检查给定的这个阶段下该Contributor是否还有未进行处理的ConfigDataLocation
     *
     * @param importPhase 当前阶段
     * @return 如果还有未进行处理的ConfigDataLocation, 那么return true; 否则return false
     */
    open fun hasUnprocessedImports(importPhase: ImportPhase): Boolean {
        if (getImports().isEmpty()) {
            return false
        }

        // 如果children当中包含了给定的ImportPhase, 那么说明该ImportPhase已经处理过了, 不应该去进行再次处理
        return !children.containsKey(importPhase)
    }

    /**
     * 获取ConfigDataProperties当中已经导入的ConfigDataLocation
     *
     * @return ConfigDataLocations(如果为空的话, return empty list)
     */
    open fun getImports(): List<ConfigDataLocation> = configDataProperties?.imports ?: Collections.emptyList()

    /**
     * 根据ImportPhase导入阶段去获取到该阶段对应的所有的Contributor
     *
     * @param importPhase ImportPhase
     * @return 该阶段的所有的Contributor的列表
     */
    open fun getChildren(importPhase: ImportPhase): List<ConfigDataEnvironmentContributor> =
        this.children[importPhase] ?: emptyList()

    /**
     * 获取到导入的Resource
     *
     * @return ConfigDataResource(or null)
     */
    @Nullable
    open fun getResource(): ConfigDataResource? = this.resource

    /**
     * 基于给定ImportPhase阶段的Children Contributors, 去构建一个新的Contributor
     *
     * @param importPhase 当前所处的阶段(激活Profile之前/之后)
     * @param children 该阶段的Children Contributors
     * @return 根据新的children, 去构建出来的新的Contributor对象
     */
    open fun withChildren(
        importPhase: ImportPhase,
        children: List<ConfigDataEnvironmentContributor>
    ): ConfigDataEnvironmentContributor {
        val updatedChildren = LinkedHashMap<ImportPhase, List<ConfigDataEnvironmentContributor>>(this.children)

        // 替换当前阶段ImportPhase的Children(这样在迭代Contributor的过程当中, 下次就不会再迭代到这个Contributor了...)
        updatedChildren[importPhase] = children

        // 如果当前是当Profile已经生效的阶段的话...
        if (importPhase == ImportPhase.AFTER_PROFILE_ACTIVATION) {
            moveProfileSpecific(updatedChildren)
        }

        val contributor = ConfigDataEnvironmentContributor(this.kind)
        contributor.location = this.location
        contributor.profileSpecific = this.profileSpecific
        contributor.configurationPropertySource = this.configurationPropertySource
        contributor.propertySource = this.propertySource
        contributor.resource = this.resource
        contributor.children = updatedChildren
        contributor.configDataProperties = this.configDataProperties
        return contributor
    }

    private fun moveProfileSpecific(children: MutableMap<ImportPhase, List<ConfigDataEnvironmentContributor>>) {
        // TODO
    }

    /**
     * 替换当前Contributor的children当中的Contributor(将existing替换replacement)
     *
     * @param existing 已经存在的Contributor Node, 这个Node需要去进行替换
     * @param replacement 要去进行existing去替换成为的目标Contributor Node
     * @return 执行替换children之后的新Contributor对象
     */
    open fun withReplacement(
        existing: ConfigDataEnvironmentContributor,
        replacement: ConfigDataEnvironmentContributor
    ): ConfigDataEnvironmentContributor {
        // 如果当前的Contributor就是想要去进行替换的Contributor的话, 那么返回replacement,
        // 实现将existing替换为replacement
        if (this === existing) {
            return replacement
        }
        val updatedChildren = LinkedHashMap(this.children)

        // 执行递归, 如果children当中遇到了existing这个Contributor的话, 把它替换成为replacement这个Contributor
        for (entry in this.children) {
            val importPhase = entry.key
            val contributors = entry.value
            val updatedContributors = ArrayList<ConfigDataEnvironmentContributor>()

            // 递归children, 去执行替换...
            for (contributor in contributors) {
                updatedContributors.add(contributor.withReplacement(existing, replacement))
            }

            // 为当前阶段聚合成为一个列表...替换掉之前的children列表
            updatedChildren[importPhase] = Collections.unmodifiableList(updatedContributors)
        }

        // 创建一个新的Contributor, 把替换之后得到的children拷贝过去...
        val contributor = ConfigDataEnvironmentContributor(this.kind)
        contributor.children = updatedChildren
        contributor.profileSpecific = this.profileSpecific
        contributor.configDataProperties = this.configDataProperties
        contributor.resource = this.resource
        contributor.location = this.location
        contributor.propertySource = this.propertySource
        contributor.configurationPropertySource = this.configurationPropertySource
        return contributor
    }

    /**
     * 创建一个新的ConfigDataEnvironmentContributor
     *
     * @param contributors Contributors
     * @param activationContext ActivationContext
     * @return 新的Contributor(kind=BOUND_IMPORT)
     */
    open fun withBoundProperties(
        contributors: Iterable<ConfigDataEnvironmentContributor>,
        @Nullable activationContext: ConfigDataActivationContext?
    ): ConfigDataEnvironmentContributor {
        val contributor = ConfigDataEnvironmentContributor(Kind.BOUND_IMPORT)
        contributor.profileSpecific = this.profileSpecific
        contributor.configDataProperties = this.configDataProperties
        contributor.resource = this.resource
        contributor.location = this.location
        contributor.propertySource = this.propertySource
        contributor.configurationPropertySource = this.configurationPropertySource
        return contributor
    }

    override fun toString(): String = kind.toString()


    /**
     * Contributor的迭代器
     */
    private inner class ContributorIterator : Iterator<ConfigDataEnvironmentContributor> {

        /**
         * 导入阶段
         */
        @Nullable
        private var phase: ImportPhase? = ImportPhase.AFTER_PROFILE_ACTIVATION

        /**
         * 该阶段的Children Contributor
         */
        private var children = getChildren(phase!!).iterator()

        /**
         * 当前正在处理的Contributor
         */
        private var current: Iterator<ConfigDataEnvironmentContributor> = Collections.emptyIterator()

        /**
         * next
         */
        @Nullable
        private var next: ConfigDataEnvironmentContributor? = null

        override fun hasNext(): Boolean = fetchIfNecessary() != null

        override fun next(): ConfigDataEnvironmentContributor {
            val next = fetchIfNecessary() ?: throw NoSuchElementException()
            this.next = null
            return next
        }

        @Nullable
        private fun fetchIfNecessary(): ConfigDataEnvironmentContributor? {
            if (this.next != null) {
                return this.next
            }
            if (this.current.hasNext()) {
                this.next = this.current.next()
                return this.next
            }
            if (this.children.hasNext()) {
                this.current = this.children.next().iterator()
                return fetchIfNecessary()
            }
            if (this.phase == ImportPhase.AFTER_PROFILE_ACTIVATION) {
                this.phase = ImportPhase.BEFORE_PROFILE_ACTIVATION
                this.children = getChildren(this.phase!!).iterator()
                return fetchIfNecessary()
            }
            if (this.phase == ImportPhase.BEFORE_PROFILE_ACTIVATION) {
                this.phase = null
                this.next = this@ConfigDataEnvironmentContributor
                return this.next
            }
            return null
        }
    }

    /**
     * 配置文件导入的枚举值
     */
    enum class ImportPhase {

        /**
         * 在profiles导入之前的阶段
         */
        BEFORE_PROFILE_ACTIVATION,

        /**
         * 在profiles已经导入完成的阶段
         */
        AFTER_PROFILE_ACTIVATION;

        companion object {
            @JvmStatic
            fun get(@Nullable activationContext: ConfigDataActivationContext?): ImportPhase {
                if (activationContext?.profiles != null) {
                    return AFTER_PROFILE_ACTIVATION
                }
                return BEFORE_PROFILE_ACTIVATION
            }
        }
    }

    /**
     * Contributor的类型
     */
    enum class Kind {
        ROOT,
        INITIAL,
        EXISTING,
        UNBOUND_IMPORT,
        BOUND_IMPORT,
        EMPTY_LOCATION
    }

    companion object {

        /**
         * 根据初始化要去进行导入的ConfigDataLocation, 去创建[ConfigDataEnvironmentContributor]
         *
         * @param initialImport 初始化要去进行导入的ConfigDataLocation
         * @return ConfigDataEnvironmentContributor, Kind=INITIAL
         */
        @JvmStatic
        fun ofInitialImport(initialImport: ConfigDataLocation): ConfigDataEnvironmentContributor {
            val configDataProperties = ConfigDataProperties(listOf(initialImport))
            return ConfigDataEnvironmentContributor(configDataProperties)
        }

        /**
         * 根据已经存在的PropertySource去创建[ConfigDataEnvironmentContributor]
         *
         * @param propertySource PropertySource
         * @return ConfigDataEnvironmentContributor, Kind=EXISING
         */
        @JvmStatic
        fun forExisting(propertySource: PropertySource<*>): ConfigDataEnvironmentContributor {
            return ConfigDataEnvironmentContributor(propertySource)
        }

        @JvmStatic
        fun of(contributors: List<ConfigDataEnvironmentContributor>): ConfigDataEnvironmentContributor {
            val children = LinkedHashMap<ImportPhase, List<ConfigDataEnvironmentContributor>>()
            children[ImportPhase.BEFORE_PROFILE_ACTIVATION] = contributors
            return ConfigDataEnvironmentContributor(children)
        }

        /**
         * 创建一个UnboundImport的Contributor, 这个Contributor是被别的Contributor所导入进来的,
         * 并且未来还可能去导入一些新的Contributor
         *
         * @param location 这个Contributor的ConfigDataLocation
         * @param resource ConfigDataResource
         * @param profileSpecific 这个Contributor是否是从一个给定Profile的情况下去进行导入的
         * @param configData ConfigData, 维护导入配置文件的结果PropertySource
         * @param propertySourceIndex 要去用于构建Contributor的PropertySource的index, 用于从ConfigData当中去获取PropertySource
         * @return 根据给定的条件, 去创建出来的新的Contributor实例(Kind=UNBOUND_IMPORT)
         */
        @JvmStatic
        fun ofUnboundImport(
            location: ConfigDataLocation,
            resource: ConfigDataResource,
            profileSpecific: Boolean,
            configData: ConfigData,
            propertySourceIndex: Int
        ): ConfigDataEnvironmentContributor {
            val contributor = ConfigDataEnvironmentContributor(Kind.UNBOUND_IMPORT)
            contributor.location = location
            contributor.resource = resource
            contributor.profileSpecific = profileSpecific
            val propertySource = configData.propertySources[propertySourceIndex]
            val configurationPropertySource = ConfigurationPropertySource.from(propertySource)
            contributor.propertySource = propertySource
            contributor.configurationPropertySource = configurationPropertySource
            return contributor
        }

        /**
         * 为一个空的ConfigDataLocation去创建Contributor, 根据这个ConfigDataLocation, 无法去获取到PropertySource, 因此为EMPTY_LOCATION
         *
         * @param location 这个Contributor的ConfigDataLocation
         * @param profileSpecific 这个Contributor是否是从一个给定Profile的情况下去进行导入的
         * @return 创建出来的新的符合给定的条件的Contributor(Kind=EMPTY_LOCATION)
         */
        @JvmStatic
        fun ofEmptyLocation(location: ConfigDataLocation, profileSpecific: Boolean): ConfigDataEnvironmentContributor {
            val contributor = ConfigDataEnvironmentContributor(Kind.EMPTY_LOCATION)
            contributor.location = location
            contributor.profileSpecific = profileSpecific
            return contributor
        }
    }
}