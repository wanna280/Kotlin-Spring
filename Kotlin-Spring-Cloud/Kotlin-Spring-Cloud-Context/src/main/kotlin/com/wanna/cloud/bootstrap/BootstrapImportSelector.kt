package com.wanna.cloud.bootstrap

import com.wanna.framework.context.annotation.DeferredImportSelector
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.util.StringUtils

/**
 * 这是一个Bootstrap的ImportSelector，负责给Spring的Bootstrap容器当中导入组件
 *
 * @see BootstrapImportSelectorConfiguration
 * @see BootstrapApplicationListener
 */
open class BootstrapImportSelector : DeferredImportSelector, EnvironmentAware {

    private var environment: Environment? = null

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    /**
     * 从SpringFactories当中去加载到通过BootstrapConfiguration去配置的配置类，导入到容器当中
     *
     * @param metadata metadata
     * @return BootstrapConfiguration导入的相关配置类，会被自动导入到Bootstrap容器当中
     */
    override fun selectImports(metadata: AnnotationMetadata): Array<String> {
        val bootstrapConfigurations =
            SpringFactoriesLoader.loadFactoryNames(BootstrapConfiguration::class.java).toMutableList()
        // 从Environment当中去解析Bootstrap的sources...
        val sources = this.environment!!.getProperty("spring.cloud.bootstrap.sources", "")!!
        bootstrapConfigurations += StringUtils.commaDelimitedListToStringArray(sources)
        return bootstrapConfigurations.toTypedArray()
    }
}