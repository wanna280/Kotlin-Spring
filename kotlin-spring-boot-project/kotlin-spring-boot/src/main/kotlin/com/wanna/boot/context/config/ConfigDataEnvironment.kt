package com.wanna.boot.context.config

import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.io.ResourceLoader

open class ConfigDataEnvironment(
    private val environment: ConfigurableEnvironment,
    private val resourceLoader: ResourceLoader,
    private val additionalProfiles: Collection<String>
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
    }

    open fun processAndApply() {
        val resolver = StandardConfigDataLocationResolver(environment)
        val propertySources = this.environment.getPropertySources()

        resolver.propertySourceLoaders.forEach { loader ->
            resolver.getConfigNames().forEach { configName ->
                loader.getFileExtensions().forEach { extensions ->
                    val configLocation = "$configName.$extensions"
                    val name = "Config resource [$configLocation] via location [$configLocation]"
                    val sources = loader.load(name, this.resourceLoader.getResource(configLocation))
                    if (sources.isNotEmpty()) {
                        sources.forEach {
                            if (it.source is Map<*, *> && (it.source as Map<*, *>).isNotEmpty()) {
                                propertySources.addLast(it)
                            }
                        }
                    }
                }
            }
        }
    }

}