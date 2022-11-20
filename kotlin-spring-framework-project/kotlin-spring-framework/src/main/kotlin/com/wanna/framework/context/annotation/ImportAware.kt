package com.wanna.framework.context.annotation

import com.wanna.framework.context.aware.Aware
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是一个ImportAware，可以注入通过@Import的配置类的相关信息
 * 例如：如果A通过@Import注解导入了B，那么B就可以通过ImportAware获取到A的相关元信息
 */
fun interface ImportAware : Aware {
    /**
     * 注入通过@Import导入当前配置类的配置类的注解信息
     *
     * @param annotationMetadata 配置类注解信息
     */
    fun setImportMetadata(annotationMetadata: AnnotationMetadata)
}