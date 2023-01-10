package com.wanna.boot.context.config

import com.wanna.boot.ConfigurableBootstrapContext
import com.wanna.boot.context.config.ConfigDataEnvironmentContributor.Kind.UNBOUND_IMPORT
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.boot.context.properties.source.ConfigurationPropertySource
import com.wanna.framework.lang.Nullable
import org.slf4j.LoggerFactory
import java.util.*
import java.util.stream.Collectors

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @param bootstrapContext BootstrapContext
 * @param root root Contributor
 */
class ConfigDataEnvironmentContributors(
    val bootstrapContext: ConfigurableBootstrapContext,
    val root: ConfigDataEnvironmentContributor
) : Iterable<ConfigDataEnvironmentContributor> {

    /**
     * Logger
     */
    private val logger = LoggerFactory.getLogger(ConfigDataEnvironmentContributors::class.java)

    constructor(
        bootstrapContext: ConfigurableBootstrapContext,
        contributors: List<ConfigDataEnvironmentContributor>
    ) : this(
        bootstrapContext,
        ConfigDataEnvironmentContributor.of(contributors)
    )

    /**
     * 让所有的活跃的Contributor去执行处理, 并且返回一个新的ConfigDataEnvironmentContributors实例
     *
     * @param importer ConfigDataImporter
     * @param activationContext ActivationContext
     * @return 已经执行过处理之后的ConfigDataEnvironmentContributors
     */
    fun withProcessedImports(
        importer: ConfigDataImporter,
        @Nullable activationContext: ConfigDataActivationContext?
    ): ConfigDataEnvironmentContributors {
        // 根据Context当中是否包含于Profiles, 检查当前所处的阶段是在激活profiles之前还是之后
        val importPhase = ConfigDataEnvironmentContributor.ImportPhase.get(activationContext)

        var result: ConfigDataEnvironmentContributors = this
        var processed = 0

        // 遍历root Contributor当中的所有的活跃的Contributor, 去进行处理...
        while (true) {
            // 获取下一个要去进行处理的Contributor
            val contributor = getNextToProcess(result, activationContext, importPhase)

            // 如果已经没有要去进行继续处理的Contributor了, 说明已经是最后一个Contributor了, 直接return result
            if (contributor == null) {
                if (logger.isTraceEnabled) {
                    logger.trace("Processed imports for of $processed contributors")
                }
                return result
            }

            // 如果kind=UNBOUND_IMPORT, 说明是一个新导入进来的配置文件所产生的Contributor, 它需要完成绑定...
            // 那么需要将rootContributor当中的, 当前的Contributor(kind=UNBOUND_IMPORT)去替换成为bound Contributor(kind=BOUND_IMPORT)
            if (contributor.kind == UNBOUND_IMPORT) {

                // 利用当前Contributor的PropertySource构建Binder, 并对ConfigDataProperties去完成绑定
                // 主要就是计算得到当前Contributor要去进行递归导入的ConfigDataLocation, 以及要去进行激活的Profiles
                val bound = contributor.withBoundProperties(result, activationContext)

                // 将kind=UNBOUND_IMPORT状态的Contributor去替换成为已经绑定之后kind=BOUND_IMPORT的Contributor
                result = ConfigDataEnvironmentContributors(
                    this.bootstrapContext, result.root.withReplacement(contributor, bound)
                )
                continue
            }

            // 构建ConfigDataLocationResolver进行ConfigDataLocation的解析时, 需要用到的Context信息
            val resolverContext = ContributorConfigDataLocationResolverContext(result, contributor, activationContext)
            // 构建ContributorDataLoader进行配置文件的加载时, 需要用到的Context信息
            val configDataLoaderContext = ContributorDataLoaderContext(this)

            // 获取到当前Contributor需要去进行导入的ConfigDataLocation
            val imports = contributor.getImports()

            // 执行加载和解析配置文件...
            val imported = importer.resolveAndLoad(activationContext, resolverContext, configDataLoaderContext, imports)

            // 将该Contributor导入进来的配置文件的PropertySource, 去进行分别转换成为Contributor, 并放入到当前Contributor的children当中...
            // 这样下次迭代的时候, 就能找到该kind=UNBOUND_IMPORT的那些Contributor, 去处理绑定, 从而递归计算导入的配置文件...
            val contributorAndChildren = contributor.withChildren(importPhase, asContributors(imported))

            // 将rootContributor的children当中的Contributor去替换成为contributorAndChildren...
            result = ConfigDataEnvironmentContributors(
                this.bootstrapContext, result.root.withReplacement(contributor, contributorAndChildren)
            )
            processed++
        }
    }

    /**
     * 将给定的配置文件的导出结果, 去转换成为Contributor, 对于新导入进来的配置文件, 状态都是UNBOUND_IMPORT
     *
     * @param imported 使用DataImporter去执行导入配置文件的结果
     * @return list of Contributor(type=UNBOUND_IMPORT)
     */
    private fun asContributors(imported: Map<ConfigDataResolutionResult, ConfigData>): List<ConfigDataEnvironmentContributor> {
        val contributors = ArrayList<ConfigDataEnvironmentContributor>(imported.size * 5)
        for (entry in imported) {
            val (resource, location, profileSpecific) = entry.key
            val configData = entry.value

            // 如果该ConfigData的PropertySources为空的话, 那么构建一个kind=EMPTY_LOCATION的Contributor
            if (configData.propertySources.isEmpty()) {
                contributors.add(ConfigDataEnvironmentContributor.ofEmptyLocation(location, profileSpecific))

                // 为所有的PropertySource去构建Contributor, kind=UNBOUND_IMPORT
            } else {
                for (index in configData.propertySources.indices.reversed()) {
                    val contributor = ConfigDataEnvironmentContributor.ofUnboundImport(
                        location, resource, profileSpecific, configData, index
                    )
                    contributors.add(contributor)
                }
            }
        }
        return contributors
    }

    /**
     * 获取到Contributors当中下一个要去进行处理的Contributor
     *
     * @param contributors Contributors
     * @param activationContext ActivationContext
     * @param importPhase 正在执行的导入阶段
     * @return 下一个进行要去进行处理的Contributor(如果不存在有下一个了, 那么return null)
     */
    @Nullable
    private fun getNextToProcess(
        contributors: ConfigDataEnvironmentContributors,
        @Nullable activationContext: ConfigDataActivationContext?,
        importPhase: ConfigDataEnvironmentContributor.ImportPhase
    ): ConfigDataEnvironmentContributor? {

        // 遍历root当中的当前所有children Contributor
        for (contributor in contributors.root) {

            // 1.如果kind=UNBOUND_IMPORT, 那么该Contributor一定需要去进行处理, 因为它是一个新导入进来的配置文件...
            // 2.如果kind不是UNBOUND_IMPORT的话, 那么需要检查它是否还有未处理的导入的情况? (ConfigDataProperties.imports不为空)
            if (contributor.kind == UNBOUND_IMPORT
                || isActiveWithUnprocessedImports(activationContext, importPhase, contributor)
            ) {
                return contributor
            }
        }
        return null
    }

    /**
     * 该Contributor是否还有未进行处理的ConfigDataLocation?
     *
     * @param activationContext ActivationContext
     * @param importPhase 当前所处的阶段
     * @param contributor Contributor
     * @return 如果还有未进行处理的配置文件, 那么return true; 否则return false
     */
    private fun isActiveWithUnprocessedImports(
        activationContext: ConfigDataActivationContext?,
        importPhase: ConfigDataEnvironmentContributor.ImportPhase,
        contributor: ConfigDataEnvironmentContributor
    ): Boolean {
        return contributor.isActive(activationContext) && contributor.hasUnprocessedImports(importPhase)
    }

    /**
     * 获取迭代Contributor的迭代器
     *
     * @return Iterator of Contributor
     */
    override fun iterator(): Iterator<ConfigDataEnvironmentContributor> = this.root.iterator()

    /**
     * 获取到当前Contributors的Binder
     *
     * @param activationContext ActivationContext
     * @return Binder
     */
    @Suppress("UNCHECKED_CAST")
    fun getBinder(@Nullable activationContext: ConfigDataActivationContext?): Binder {
        // 获取到所有的Contributor当中的ConfigurationPropertySource...
        val propertySources = this.root.stream()
            .filter(ConfigDataEnvironmentContributor::hasConfigurationPropertySource)
            .map(ConfigDataEnvironmentContributor::getConfigurationPropertySource)
            .filter(Objects::nonNull).collect(Collectors.toList()) as List<ConfigurationPropertySource>

        val placeholdersResolver =
            ConfigDataEnvironmentContributorPlaceholdersResolver(this.root, activationContext, null, false)
        // 根据这些PropertySources, 去构建出来Binder...
        return Binder(propertySources, placeholdersResolver, emptyList(), null, null)
    }

    private class ContributorDataLoaderContext(private val contributors: ConfigDataEnvironmentContributors) :
        ConfigDataLoaderContext {
        override fun getBootstrapContext(): ConfigurableBootstrapContext = contributors.bootstrapContext
    }

    private class ContributorConfigDataLocationResolverContext(
        private val contributors: ConfigDataEnvironmentContributors,
        private val contributor: ConfigDataEnvironmentContributor,
        @Nullable private val activationContext: ConfigDataActivationContext?
    ) : ConfigDataLocationResolverContext {

        @Volatile
        @Nullable
        private var binder: Binder? = null

        override fun getBinder(): Binder {
            if (this.binder == null) {
                this.binder = contributors.getBinder(activationContext)
            }
            return this.binder!!
        }

        @Nullable
        override fun getParent(): ConfigDataResource? = contributor.getResource()

        override fun getBootstrapContext(): ConfigurableBootstrapContext = this.contributors.bootstrapContext
    }

    enum class BinderOption {
        FAIL_ON_BIND_TO_INACTIVE_SOURCE
    }
}