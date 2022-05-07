package com.wanna.framework.context.annotation

import com.wanna.framework.context.aware.Aware
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是一个ImportAware，可以注入@Import的相关信息
 * 例如：如果A通过@Import注解导入了B，那么B就可以通过ImportAware获取到A的相关元信息
 */
interface ImportAware : Aware {
    // TODO
    fun setImportMetadata(annotationMetadata: AnnotationMetadata)
}