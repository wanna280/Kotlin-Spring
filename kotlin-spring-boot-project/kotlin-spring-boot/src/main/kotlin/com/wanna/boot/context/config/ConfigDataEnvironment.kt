package com.wanna.boot.context.config

import com.wanna.boot.ConfigurableBootstrapContext
import com.wanna.boot.DefaultPropertiesPropertySource
import com.wanna.boot.context.properties.bind.Bindable
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.MutablePropertySources
import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.lang.Nullable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 通过包装一个[ConfigurableEnvironment]去进行实现, 它可以被用作导入/应用[ConfigData];
 * 通过[ConfigurableEnvironment]当中的[PropertySource]和一些初始化的位置, 去包装成为一个[ConfigDataEnvironmentContributors]对象;
 *
 * 对于配置文件的初始位置, 可以通过[LOCATION_PROPERTY]/[ADDITIONAL_LOCATION_PROPERTY]/[IMPORT_PROPERTY]这些属性值去进行导入,
 * 如果没有明确给定这些属性值, 那么将会默认使用[DEFAULT_SEARCH_LOCATIONS]去作为默认的搜索路径
 *
 * @param environment Environment
 * @param resourceLoader ResourceLoader
 * @param bootstrapContext BootstrapContext
 * @param additionalProfiles 要额外应用的Profiles
 * @param environmentUpdateListener 监听[Environment]当中的PropertySource/Profiles的变更的Listener
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/7
 */
open class ConfigDataEnvironment(
    private val environment: ConfigurableEnvironment,
    private val resourceLoader: ResourceLoader,
    private val additionalProfiles: Collection<String>,
    private val bootstrapContext: ConfigurableBootstrapContext,
    @Nullable environmentUpdateListener: ConfigDataEnvironmentUpdateListener? = null
) {
    companion object {

        /**
         * 配置文件的位置
         */
        private const val LOCATION_PROPERTY = "spring.config.location"

        /**
         * 额外的配置文件的路径
         */
        private const val ADDITIONAL_LOCATION_PROPERTY = "spring.config.additional-location"

        /**
         *  import额外的配置文件的路径
         */
        private const val IMPORT_PROPERTY = "spring.config.import"

        /**
         * Array<ConfigDataLocation>的Bindable
         */
        @JvmStatic
        private val CONFIG_DATA_LOCATION_ARRAY: Bindable<Array<ConfigDataLocation>> =
            Bindable.of(Array<ConfigDataLocation>::class.java)

        /**
         * 空的ConfigDataLocation数组
         */
        @JvmStatic
        private val EMPTY_LOCATIONS = emptyArray<ConfigDataLocation>()

        /**
         * 默认的搜索ConfigData的位置
         */
        @JvmStatic
        private val DEFAULT_SEARCH_LOCATIONS: Array<ConfigDataLocation> = arrayOf(
            // location of "classpath:/", "classpath:/config/", !!!!!! non "classpath:/config/*/"
            ConfigDataLocation.of("optional:classpath:/;optional:classpath:/config/")!!,

            // location of "file:./", "file:./config/", "file:config/*/"
            ConfigDataLocation.of("optional:file:./;optional:file:./config/;optional:file:./config/*/")!!
        )
    }

    /**
     * Logger
     */
    private val logger: Logger = LoggerFactory.getLogger(ConfigDataEnvironment::class.java)

    /**
     * Binder
     */
    private val binder: Binder = Binder.get(environment)

    /**
     * 根据Binder&Environment去创建出来初始化的Contributors
     */
    private val contributors = this.createContributors(binder)

    /**
     * 从SpringFactories当中去获取到所有的ConfigDataLoader, 去提供配置文件的加载
     */
    private val loaders = ConfigDataLoaders(bootstrapContext)

    /**
     * 从SpringFactories当中去获取到所有的[ConfigDataLocationResolver], 去提供配置文件的Location的解析
     */
    private val resolvers = this.createConfigDataLocationResolvers(bootstrapContext, binder, resourceLoader)

    /**
     * Environment发生变更的Listener, 如果没有指定的话, 那么使用NONE, 不做任何的处理
     */
    private val environmentUpdateListener: ConfigDataEnvironmentUpdateListener =
        environmentUpdateListener ?: ConfigDataEnvironmentUpdateListener.NONE

    /**
     * 使用所有的contributors去进行处理, 并将处理结果当中的新产生的PropertySource添加到[Environment]当中
     *
     * @see ConfigDataEnvironmentContributors
     * @see ConfigDataEnvironmentContributor
     */
    open fun processAndApply() {
        // ConfigData Importer, 封装ConfigDataLoaders&ConfigDataLocationResolvers执行真正的配置文件的解析加载
        val importer = ConfigDataImporter(loaders, resolvers)

        // 1.利用Contributors去进行初始化的处理(初始化一些配置文件的导入)
        var contributors = processInitial(this.contributors, importer)

        // 创建ActivationContext
        var activationContext = createActivationContext(contributors.getBinder(null))

        // 2.在没有Profiles的情况下, 去进行处理...(这块主要是进行CloudPlatform的检查, 第二次处理, 在正常情况下不会有任何的作用)
        contributors = processWithoutProfiles(contributors, importer, activationContext)

        // 给ActivationContext去补充上Profiles
        activationContext = withProfiles(contributors, activationContext)

        // 3.在有Profiles的情况下, 再次去进行处理(这块就是主要处理Profiles的配置文件的导入的...)
        contributors = processWithProfiles(contributors, importer, activationContext)

        // 将Contributors当中的结果应用给Environment当中...
        applyToEnvironment(contributors, activationContext, importer.loadedLocations, importer.optionalLocations)

    }

    /**
     * 创建ConfigDataEnvironmentContributors
     *
     * * 1.为所有的PropertySource去创建Kind=EXISTING的Contributor
     * * 2.为所有的要去进行额外导入的配置文件去创建Kind=INITIAL的Contributor
     * * 3.将Kind=EXISTING和Kind=INITIAL的Contributor去封装成为一个Kind=ROOT的Contributor
     *
     * @param binder Binder
     * @return ConfigDataEnvironmentContributors
     */
    private fun createContributors(binder: Binder): ConfigDataEnvironmentContributors {
        if (logger.isTraceEnabled) {
            logger.trace("Building config data environment contributors")
        }
        val propertySources = this.environment.getPropertySources()
        val contributors = ArrayList<ConfigDataEnvironmentContributor>()

        // 为所有的PropertySource去构建出来对应的Contributor...
        for (propertySource in propertySources) {
            if (logger.isTraceEnabled) {
                logger.trace("Creating wrapped config data contributor for '${propertySource.name}'")
            }
            contributors.add(ConfigDataEnvironmentContributor.forExisting(propertySource))
        }

        // 添加所有的InitialImport的Contributor
        contributors.addAll(getInitialImportContributors(binder))

        // 根据这些Contributor, 去创建出来root Contributor并封装成为Contributors
        return createContributors(contributors)
    }

    /**
     * 创建[ConfigDataLocationResolvers], 去扫描所有的[ConfigDataLocationResolver]去提供对于location的解析
     *
     * @param binder Binder
     * @param bootstrapContext BootstrapContext
     * @param resourceLoader ResourceLoader
     */
    protected open fun createConfigDataLocationResolvers(
        bootstrapContext: ConfigurableBootstrapContext,
        binder: Binder,
        resourceLoader: ResourceLoader
    ): ConfigDataLocationResolvers {
        return ConfigDataLocationResolvers(bootstrapContext, binder, resourceLoader)
    }

    /**
     * 获取所有的InitialImport类型的Contributor, 主要用于对一些初始化要去进行导入的配置文件去进行添加Contributor
     *
     * @param binder Binder
     * @return 所有的InitialImport类型的Contributor
     */
    private fun getInitialImportContributors(binder: Binder): List<ConfigDataEnvironmentContributor> {
        val initialContributors = ArrayList<ConfigDataEnvironmentContributor>()

        // 添加"spring.config.import"对应的Contributor, 没有默认值
        addInitialImportContributors(initialContributors, bindLocations(binder, IMPORT_PROPERTY, EMPTY_LOCATIONS))

        // 添加"spring.config.additional-location"对应的Contributor, 没有默认值
        addInitialImportContributors(
            initialContributors,
            bindLocations(binder, ADDITIONAL_LOCATION_PROPERTY, EMPTY_LOCATIONS)
        )

        // 添加spring.config.location对应的Contributor, 如果没有指定的话, 那么有默认值
        addInitialImportContributors(
            initialContributors,
            bindLocations(binder, LOCATION_PROPERTY, DEFAULT_SEARCH_LOCATIONS)
        )
        return initialContributors
    }

    /**
     * 添加所有的初始化导入的Contributor
     *
     * @param initialContributors initialContributors, 最终创建的Contributor需要收集到这里
     * @param locations 初始化导入的配置文件的路径列表
     */
    private fun addInitialImportContributors(
        initialContributors: MutableList<ConfigDataEnvironmentContributor>,
        locations: Array<ConfigDataLocation>
    ) {
        locations.reverse() // reverse
        // 将给定的这些ConfigDataLocation都去创建一个对应的Contributor
        for (location in locations) {
            initialContributors.add(createInitialImportContributor(location))
        }
    }

    /**
     * 根据propertyName去获取到要去进行绑定的配置文件的Locations
     *
     * @param binder Binder
     * @param propertyName 属性名
     * @param other 绑定失败的默认值
     * @return 找到的ConfigDataLocations
     */
    private fun bindLocations(
        binder: Binder,
        propertyName: String,
        other: Array<ConfigDataLocation>
    ): Array<ConfigDataLocation> {
        return binder.bind(propertyName, CONFIG_DATA_LOCATION_ARRAY).orElse(other)!!
    }

    private fun createInitialImportContributor(location: ConfigDataLocation): ConfigDataEnvironmentContributor {
        return ConfigDataEnvironmentContributor.ofInitialImport(location)
    }

    /**
     * 执行对于ConfigDataEnvironmentContributors的创建
     *
     * @param contributors Contributor列表
     * @return ConfigDataEnvironmentContributors
     */
    protected open fun createContributors(contributors: List<ConfigDataEnvironmentContributor>): ConfigDataEnvironmentContributors {
        return ConfigDataEnvironmentContributors(this.bootstrapContext, contributors)
    }

    /**
     * 在初始化情况下去执行处理导入
     */
    private fun processInitial(
        contributors: ConfigDataEnvironmentContributors,
        importer: ConfigDataImporter,
    ): ConfigDataEnvironmentContributors {
        val result = contributors.withProcessedImports(importer, null)
        return result
    }

    /**
     * 在没有Profiles的情况下去执行处理导入
     */
    private fun processWithoutProfiles(
        contributors: ConfigDataEnvironmentContributors,
        importer: ConfigDataImporter,
        activationContext: ConfigDataActivationContext
    ): ConfigDataEnvironmentContributors {
        val result = contributors.withProcessedImports(importer, activationContext)
        return result
    }


    /**
     * 构建出来一个新的携带有Profiles的ActivationContext
     *
     * @param contributors contributors
     * @param activationContext 原先的没有Profiles的ActivationContext
     * @return 新的携带有Profiles的ActivationContext
     */
    private fun withProfiles(
        contributors: ConfigDataEnvironmentContributors,
        activationContext: ConfigDataActivationContext
    ): ConfigDataActivationContext {
        val binder = contributors.getBinder(activationContext)

        val additionalProfiles = LinkedHashSet(this.additionalProfiles)

        // 从"spring.profiles.include"当中去获取到要去进行额外导入的Profiles...
        additionalProfiles += getIncludedProfiles(contributors, activationContext)

        val profiles = Profiles(this.environment, binder, additionalProfiles)
        return activationContext.withProfiles(profiles)
    }

    private fun getIncludedProfiles(
        contributors: ConfigDataEnvironmentContributors,
        activationContext: ConfigDataActivationContext
    ): Collection<String> {
        // TODO
        return emptyList()
    }

    /**
     * 在已经存在有Profiles的情况下去处理导入
     */
    private fun processWithProfiles(
        contributors: ConfigDataEnvironmentContributors,
        importer: ConfigDataImporter,
        activationContext: ConfigDataActivationContext
    ): ConfigDataEnvironmentContributors {
        val result = contributors.withProcessedImports(importer, activationContext)
        return result
    }

    /**
     * 创建ConfigDataActivationContext
     *
     * @param initialBinder Binder
     * @return ConfigDataActivationContext
     */
    private fun createActivationContext(initialBinder: Binder): ConfigDataActivationContext {
        return ConfigDataActivationContext(this.environment, initialBinder)
    }

    /**
     * 将Contributors的处理结果应用给Environment当中
     *
     * @param activationContext ActivationContext
     * @param contributors Contributors
     * @param loadedLocations 已经加载的ConfigDataLocation
     * @param optionalLocations Optional ConfigDataLocations
     */
    private fun applyToEnvironment(
        contributors: ConfigDataEnvironmentContributors,
        activationContext: ConfigDataActivationContext,
        loadedLocations: Set<ConfigDataLocation>,
        optionalLocations: Set<ConfigDataLocation>
    ) {
        if (logger.isTraceEnabled) {
            logger.trace("Applying config data environment contributions")
        }
        val propertySources = this.environment.getPropertySources()
        // 将Contributor的结果应用给Environment的PropertySources当中...
        applyContributor(contributors, activationContext, propertySources)

        // 将默认的PropertySource移动到最后...
        DefaultPropertiesPropertySource.moveToEnd(propertySources)

        val profiles = activationContext.profiles ?: throw IllegalStateException("profiles cannot be null")

        // 设置DefaultProfiles
        if (logger.isTraceEnabled) {
            logger.trace("Setting default profiles: ${profiles.getDefault()}")
        }
        this.environment.setDefaultProfiles(*profiles.getDefault().toTypedArray())

        // 设置ActiveProfiles
        if (logger.isTraceEnabled) {
            logger.trace("Setting active profiles: ${profiles.getActive()}")
        }
        this.environment.setActiveProfiles(*profiles.getActive().toTypedArray())

        // 通知Listener, Profiles已经被设置
        this.environmentUpdateListener.onSetProfiles(profiles)
    }

    /**
     * 将Contributors的决策结果, 去应用到PropertySources当中
     *
     * @param contributors Contributors
     * @param activationContext ActivationContext
     * @param propertySources PropertySources
     */
    private fun applyContributor(
        contributors: ConfigDataEnvironmentContributors,
        activationContext: ConfigDataActivationContext,
        propertySources: MutablePropertySources
    ) {
        // toList, 方便展示遍历过程, 无特殊用法, 用迭代器的方式去查看太麻烦了
        // 这里迭代的时候, 似乎就已经做到了, 将有Profiles的情况排在了前面, 直接addLast就行, 无需调整位置了...
        val contributorList = contributors.toList()

        // 遍历所有的Contributor, 将它们处理的结果PropertySource应用给PropertySources当中
        for (contributor in contributorList) {
            val propertySource = contributor.getPropertySource()
            if (contributor.kind == ConfigDataEnvironmentContributor.Kind.BOUND_IMPORT && propertySource != null) {

                // 如果该Profile没有被激活, 那么...
                if (!contributor.isActive(activationContext)) {
                    if (logger.isTraceEnabled) {
                        logger.trace(String.format("Skipping inactive property source '%s'", propertySource.name))
                    }

                    // 如果该Profile需要被激活, 那么需要添加到Environment的PropertySources当中去
                } else {
                    if (logger.isTraceEnabled) {
                        logger.trace(String.format("Adding imported property source '%s'", propertySource.name))
                    }
                    propertySources.addLast(propertySource)
                    // 通知EnvironmentUpdateListener, PropertySource已经被添加到Environment当中...
                    this.environmentUpdateListener.onPropertySourceAdded(
                        propertySource,
                        contributor.getLocation()!!,
                        contributor.getResource()!!
                    )
                }
            }
        }

    }

}