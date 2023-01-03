package com.wanna.framework.core.type.filter

import com.wanna.framework.core.type.classreading.MetadataReader
import com.wanna.framework.core.type.classreading.MetadataReaderFactory

/**
 * 类型的过滤器，支持去对一个类的各个方面去进行匹配，比如匹配注解、匹配父类等方式
 *
 * @see AssignableTypeFilter
 * @see AnnotationTypeFilter
 */
fun interface TypeFilter {
    /**
     * 基于MetadataReader的方式, 去执行匹配
     *
     * @param metadataReader MetadataReader
     * @param metadataReaderFactory MetadataReaderFactory
     */
    fun matches(metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory): Boolean
}