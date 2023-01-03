package com.wanna.framework.core.annotation

import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.lang.Nullable
import java.lang.reflect.AnnotatedElement

/**
 * 注解相关的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/1
 */
object AnnotationUtils {

    /**
     * 获取给定的注解实例的AnnotationAttributes
     *
     * @param  annotation Annotation
     * @return AnnotationAttributes
     */
    @JvmStatic
    fun getAnnotationAttributes(annotation: Annotation): AnnotationAttributes =
        getAnnotationAttributes(null, annotation)

    /**
     * 获取给定的注解实例的AnnotationAttributes
     *
     * @param source source
     * @param  annotation Annotation
     * @return AnnotationAttributes
     */
    @JvmStatic
    fun getAnnotationAttributes(@Nullable source: AnnotatedElement?, annotation: Annotation): AnnotationAttributes =
        getAnnotationAttributes(source, annotation, false, false)

    /**
     * 获取给定的注解实例的AnnotationAttributes
     *
     * @param  annotation Annotation
     * @param classValueAsString 是否需要将Class转为String去进行收集
     * @param nestedAnnotationsAsMap 是否需要将内部的注解转换为Map去进行收集
     * @return AnnotationAttributes
     */
    @JvmStatic
    fun getAnnotationAttributes(
        annotation: Annotation, classValueAsString: Boolean, nestedAnnotationsAsMap: Boolean
    ): AnnotationAttributes = getAnnotationAttributes(null, annotation, classValueAsString, nestedAnnotationsAsMap)

    /**
     * 获取给定的注解实例的AnnotationAttributes
     *
     * @param source source
     * @param  annotation Annotation
     * @param classValueAsString 是否需要将Class转为String去进行收集
     * @param nestedAnnotationsAsMap 是否需要将内部的注解转换为Map去进行收集
     * @return AnnotationAttributes
     */
    @JvmStatic
    fun getAnnotationAttributes(
        @Nullable source: AnnotatedElement?,
        annotation: Annotation,
        classValueAsString: Boolean,
        nestedAnnotationsAsMap: Boolean
    ): AnnotationAttributes {
        val adapts = MergedAnnotation.Adapt.values(classValueAsString, nestedAnnotationsAsMap)
        return MergedAnnotation.from(source, annotation).asAnnotationAttributes(*adapts)
    }


    @JvmStatic
    fun handleIntrospectionFailure(@Nullable element: AnnotatedElement?, ex: Throwable) {
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