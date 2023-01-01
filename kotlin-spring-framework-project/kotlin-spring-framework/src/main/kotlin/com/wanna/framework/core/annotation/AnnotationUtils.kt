package com.wanna.framework.core.annotation

import java.lang.reflect.AnnotatedElement

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/1
 */
object AnnotationUtils {


    @JvmStatic
    fun handleIntrospectionFailure(element: AnnotatedElement?, ex: Throwable) {
        rethrowAnnotationConfigurationException(ex)
    }

    @JvmStatic
    private fun rethrowAnnotationConfigurationException(ex: Throwable) {
        if (ex is AnnotationConfigurationException) {
            throw ex
        }
    }

    /**
     * 清除AnnotationUtils相关的缓存
     */
    @JvmStatic
    fun clearCache() {
        AnnotationTypeMappings.clearCache()
        AnnotationsScanner.clearCache()
    }
}