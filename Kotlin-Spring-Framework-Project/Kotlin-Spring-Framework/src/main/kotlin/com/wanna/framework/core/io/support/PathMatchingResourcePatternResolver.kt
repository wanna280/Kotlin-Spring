package com.wanna.framework.core.io.support

import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import org.slf4j.LoggerFactory

/**
 * 支持表达式解析的ResourceLoader
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 */
open class PathMatchingResourcePatternResolver(val resourceLoader: ResourceLoader) : ResourcePatternResolver {
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