package com.wanna.boot.context.config

import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.util.StringUtils

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 */
class LocationResourceLoader(private val resourceLoader: ResourceLoader) {

    /**
     * 检查给定的Location是否含有表达式
     *
     * @param location location
     * @return 如果含有"*", return true; 否则return false
     */
    fun isPattern(location: String): Boolean = location.contains("*")

    fun getResource(location: String): Resource {
        val cleanPath = StringUtils.cleanPath(location)
        return this.resourceLoader.getResource(cleanPath)
    }
}