package com.wanna.boot.env

import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.core.io.Resource

/**
 * 这是一个PropertySource的Loader，负责去加载配置文件成为PropertySource
 *
 * @see YamlPropertySourceLoader
 * @see PropertiesPropertySourceLoader
 */
interface PropertySourceLoader {

    /**
     * 获取支持去进行处理的配置文件的后缀名列表，相当于策略接口当中的"supports"方法
     *
     * @return 支持去进行处理的配置文件后缀名列表
     */
    fun getFileExtensions(): Array<String>

    /**
     * 加载配置文件成为PropertySource
     *
     * @param name name
     * @param resource 资源路径
     * @return 加载得到的的PropertySource列表
     */
    fun load(name: String, resource: String): List<PropertySource<*>>

    /**
     * 加载配置文件成为PropertySource
     *
     * @param name name
     * @param resource 资源Resource对象
     * @return 加载得到的的PropertySource列表
     */
    fun load(name: String, resource: Resource): List<PropertySource<*>>
}