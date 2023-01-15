package com.wanna.boot.autoconfigure

import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.core.type.classreading.MetadataReader
import com.wanna.framework.core.type.classreading.MetadataReaderFactory
import com.wanna.framework.core.type.filter.TypeFilter
import com.wanna.framework.lang.Nullable

/**
 * 自动配置包的排除过滤器, 避免ComponentScan过程当中扫描到了自动配置的类
 */
open class AutoConfigurationExcludeFilter : TypeFilter {

    /**
     * 缓存的自动配置类列表
     */
    @Nullable
    private var configurations: List<String>? = null

    override fun matches(metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory): Boolean {
        return isConfiguration(metadataReader) && isAutoConfiguration(metadataReader)
    }

    open fun isAutoConfiguration(metadataReader: MetadataReader): Boolean {
        return getAutoConfigurations().contains(metadataReader.classMetadata.getClassName())
    }

    open fun isConfiguration(metadataReader: MetadataReader): Boolean {
        return metadataReader.annotationMetadata.isAnnotated(Configuration::class.java.name)
    }

    /**
     * 从SpringFactories当中去获取所有的自动配置类的列表
     *
     * @return 自动配置类列表
     */
    private fun getAutoConfigurations(): List<String> {
        if (this.configurations == null) {
            this.configurations = SpringFactoriesLoader.loadFactoryNames(EnableAutoConfiguration::class.java)
        }
        return this.configurations!!
    }
}