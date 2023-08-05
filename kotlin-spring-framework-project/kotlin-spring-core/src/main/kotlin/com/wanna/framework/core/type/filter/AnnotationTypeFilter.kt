package com.wanna.framework.core.type.filter

import com.wanna.framework.core.type.classreading.MetadataReader
import com.wanna.framework.core.type.classreading.MetadataReaderFactory


/**
 * 匹配注解, 判断一个类上是否标注了某个注解? 
 *
 * @param annotationType 要去匹配的注解类型
 */
open class AnnotationTypeFilter(private val annotationType: Class<out Annotation>) : TypeFilter {
    override fun matches(metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory): Boolean {
        return metadataReader.annotationMetadata.isAnnotated(annotationType.name)
    }
}