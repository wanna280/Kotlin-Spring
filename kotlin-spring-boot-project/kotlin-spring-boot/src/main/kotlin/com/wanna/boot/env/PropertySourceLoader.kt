package com.wanna.boot.env

import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.core.io.Resource

/**
 * 提供对于[PropertySource]的加载的Loader, 负责去加载配置文件的[Resource]资源去成为[PropertySource]
 *
 * @see YamlPropertySourceLoader
 * @see PropertiesPropertySourceLoader
 */
interface PropertySourceLoader {

    /**
     * 获取支持去进行处理的配置文件的后缀名列表, 相当于策略接口当中的"supports"方法
     *
     * @return 支持去进行处理的配置文件后缀名列表(例如"properties"/"xml"/"yaml"/"yml")
     */
    fun getFileExtensions(): Array<String>

    /**
     * 将给定的[Resource]配置文件去加载成为[PropertySource]
     *
     * @param name PropertySource name
     * @param resource 资源[Resource]
     * @return 根据给定的[Resource]去加载得到的的PropertySource列表
     */
    fun load(name: String, resource: Resource): List<PropertySource<*>>
}