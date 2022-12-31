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

    private fun rethrowAnnotationConfigurationException(ex: Throwable) {
        if (ex is AnnotationConfigurationException) {
            throw ex
        }
    }
}