package com.wanna.cloud.bootstrap

import com.wanna.framework.context.annotation.DeferredImportSelector
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.util.StringUtils

/**
 * 这是一个Bootstrap的ImportSelector, 负责给SpringCloud的Bootstrap容器当中导入SpringBean组件
 *
 * @see BootstrapImportSelectorConfiguration
 * @see BootstrapApplicationListener
 */
open class BootstrapImportSelector : DeferredImportSelector, EnvironmentAware {

    /**
     * Environment
     */
    private var environment: Environment? = null

    /**
     * 从SpringFactories当中去加载到通过BootstrapConfiguration去配置的配置类, 导入到容器当中
     *
     * @param metadata 导入这个Selector的注解元信息metadata(这里用不上, 不关心)
     * @return BootstrapConfiguration导入的相关配置类, 会被自动导入到Bootstrap容器当中
     */
    override fun selectImports(metadata: AnnotationMetadata): Array<String> {
        val bootstrapConfigurations =
            SpringFactoriesLoader.loadFactoryNames(BootstrapConfiguration::class.java).toMutableList()

        // 从Environment当中去解析Bootstrap容器需要额外使用的配置类sources...
        val sources = getEnvironment().getProperty("spring.cloud.bootstrap.sources", "")
        bootstrapConfigurations += StringUtils.commaDelimitedListToStringArray(sources)
        return bootstrapConfigurations.toTypedArray()
    }


    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    open fun getEnvironment(): Environment {
        return this.environment
            ?: throw IllegalStateException("Environment in BootstrapImportSelector is not available")
    }

}