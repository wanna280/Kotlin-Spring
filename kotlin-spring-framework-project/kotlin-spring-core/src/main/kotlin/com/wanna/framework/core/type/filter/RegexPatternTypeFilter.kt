package com.wanna.framework.core.type.filter

import com.wanna.framework.core.type.classreading.MetadataReader
import com.wanna.framework.core.type.classreading.MetadataReaderFactory
import java.util.regex.Pattern

/**
 * 基于正则表达式的匹配的[TypeFilter], 去对一个目标类的类名, 使用正则表达式的方式去进行匹配去进行匹配
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/21
 *
 * @param pattern 用于去进行匹配的正则表达式Pattern
 */
open class RegexPatternTypeFilter(private val pattern: Pattern) : TypeFilter {

    /**
     * 利用正则表达式, 去对目标类的元信息去进行真正的匹配
     *
     * @param metadataReader 类的元信息的读取的MetadataReader
     * @param metadataReaderFactory 根据指定的类名去创建MetadataReader的工厂方法
     */
    override fun matches(metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory): Boolean {
        return isMatchPattern(metadataReader.classMetadata.getClassName())
    }

    protected open fun isMatchPattern(className: String): Boolean = pattern.matcher(className).matches()
}