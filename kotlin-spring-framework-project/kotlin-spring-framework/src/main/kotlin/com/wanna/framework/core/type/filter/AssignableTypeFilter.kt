package com.wanna.framework.core.type.filter

import com.wanna.framework.core.type.classreading.MetadataReader
import com.wanna.framework.core.type.classreading.MetadataReaderFactory
import com.wanna.framework.util.ClassUtils

/**
 * 匹配类型，判断给定的类是否是parentClass的子类？
 *
 * @param parentClass parentClass
 */
open class AssignableTypeFilter(private val parentClass: Class<*>) : TypeFilter {
    override fun matches(clazz: Class<*>?): Boolean {
        return ClassUtils.isAssignFrom(parentClass, clazz)
    }

    override fun matches(metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory): Boolean {
        // TODO
        return super.matches(metadataReader, metadataReaderFactory)
    }
}