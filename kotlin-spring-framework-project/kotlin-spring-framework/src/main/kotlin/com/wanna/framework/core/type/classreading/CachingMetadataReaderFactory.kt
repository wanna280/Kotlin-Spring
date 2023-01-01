package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.io.DefaultResourceLoader
import com.wanna.framework.core.io.ResourceLoader
import org.springframework.core.io.Resource

/**
 * 带缓存的SimpleMetadataReaderFactory实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/1
 *
 * @param resourceLoader ResourceLoader
 */
open class CachingMetadataReaderFactory(resourceLoader: ResourceLoader) : SimpleMetadataReaderFactory(resourceLoader) {
    constructor() : this(DefaultResourceLoader())

    /**
     * MetadataReader Cache
     */
    private val metadataReaderCache: MutableMap<Resource, MetadataReader>? = null

}