package com.wanna.boot.context.config

import com.wanna.boot.ConfigurableBootstrapContext
import com.wanna.boot.DefaultPropertiesPropertySource
import com.wanna.boot.context.properties.bind.Bindable
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.MutablePropertySources
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.lang.Nullable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
    private val loaders = ConfigDataLoaders()

    /**
     * 从SpringFactories当中去获取到所有的ConfigDataLocationResolver, 去提供配置文件的路径的解析
     */
    private val resolvers = ConfigDataLocationResolvers()

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

        // 利用Contributors去进行初始化的处理
        var contributors = processInitial(this.contributors, importer)

        // 在没有Profiles的情况下, 去进行处理...
        var activationContext = createActivationContext(contributors.getBinder(null))
        contributors = processWithoutProfiles(contributors, importer, activationContext)

        // 在有profiles的情况下, 去进行处理
        activationContext = withProfiles(contributors, activationContext)
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
        return binder.bind(propertyName, CONFIG_DATA_LOCATION_ARRAY).orElse(other)
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


    private fun withProfiles(
        contributors: ConfigDataEnvironmentContributors,
        activationContext: ConfigDataActivationContext
    ): ConfigDataActivationContext {
        val binder = contributors.getBinder(activationContext)
        val profiles = Profiles(this.environment, binder, this.additionalProfiles)
        return activationContext.withProfiles(profiles)
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
        // 遍历所有的Contributor, 将它们处理的结果PropertySource应用给PropertySources当中
        for (contributor in contributors) {
            val propertySource = contributor.getPropertySource()
            if (contributor.kind == ConfigDataEnvironmentContributor.Kind.BOUND_IMPORT && propertySource != null) {
                if (!contributor.isActive(activationContext)) {
                    if (logger.isTraceEnabled) {
                        logger.trace(String.format("Skipping inactive property source '%s'", propertySource.name))
                    }
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