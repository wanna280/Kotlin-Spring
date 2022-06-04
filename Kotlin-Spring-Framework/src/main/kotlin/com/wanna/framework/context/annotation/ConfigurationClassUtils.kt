package com.wanna.framework.context.annotation

import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.type.AnnotationMetadata

object ConfigurationClassUtils {

    private val candidateIndicators = LinkedHashSet<String>()

    init {
        candidateIndicators += Component::class.java.name
        candidateIndicators += ComponentScan::class.java.name
        candidateIndicators += ImportSource::class.java.name
        candidateIndicators += Import::class.java.name
    }

    /**
     * 是否是一个候选的配置类？只需要检查它是否有@Component/@ComponentScan/@ImportSource/@Import以及@Bean方法即可
     *
     * @param metadata 目标配置类的元信息
     * @return 它是否是一个候选的配置类？
     */
    @JvmStatic
    fun isConfigurationCandidate(metadata: AnnotationMetadata): Boolean {
        if (metadata.isInterface()) {
            return false
        }

        // 检查候选的配置类的注解
        candidateIndicators.forEach {
            if (metadata.isAnnotated(it)) {
                return true
            }
        }

        try {
            return metadata.hasAnnotatedMethods(Bean::class.java.name)
        } catch (ignored: Exception) {

        }
        return false // fixed: default to return false
    }
}