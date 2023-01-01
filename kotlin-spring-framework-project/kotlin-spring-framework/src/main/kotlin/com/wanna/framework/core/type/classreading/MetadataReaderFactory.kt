package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.io.Resource

/**
 * 快速构建MetadataReader的工厂
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 *
 * @see MetadataReader
 */
interface MetadataReaderFactory {

    /**
     * 根据给定的类名, 去快速创建读取该类的元信息的MetadataReader
     *
     * @param className className
     * @return 读取该类的Class文件的读取的的MetadataReader
     */
    fun getMetadataReader(className: String): MetadataReader

    /**
     * 根据给定的Resource, 去快速构建该类的元信息的MetadataReader
     *
     * @param resource Class文件的Resource
     * @return 该Resource对应Class文件的读取的MetadataReader
     */
    fun getMetadataReader(resource: Resource): MetadataReader
}