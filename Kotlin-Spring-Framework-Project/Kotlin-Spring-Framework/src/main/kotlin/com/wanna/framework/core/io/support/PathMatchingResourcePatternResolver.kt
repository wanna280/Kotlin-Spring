package com.wanna.framework.core.io.support

import com.wanna.framework.core.io.DefaultResourceLoader
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import org.slf4j.LoggerFactory

/**
 * 支持表达式解析的ResourceLoader
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 * @param resourceLoader 执行资源加载的ResourceLoader，默认情况下使用的就是DefaultResourceLoader
 */
open class PathMatchingResourcePatternResolver(val resourceLoader: ResourceLoader = DefaultResourceLoader()) :
    ResourcePatternResolver {
    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(PathMatchingResourcePatternResolver::class.java)
    }

    override fun getResource(location: String) = resourceLoader.getResource(location)

    override fun getClassLoader(): ClassLoader? = resourceLoader.getClassLoader()

    override fun getResources(locationPattern: String): Array<Resource> {
        return arrayOf(resourceLoader.getResource(locationPattern))
    }
}