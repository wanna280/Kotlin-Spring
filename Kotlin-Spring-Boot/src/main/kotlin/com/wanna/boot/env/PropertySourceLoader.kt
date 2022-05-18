package com.wanna.boot.env

import com.wanna.framework.core.environment.PropertySource

/**
 * 这是一个PropertySource的Loader，负责去加载配置文件成为PropertySource
 */
interface PropertySourceLoader {

    /**
     * 获取配置文件的后缀名列表
     */
    fun getFileExtensions(): Array<String>

    /**
     * 加载配置文件成为PropertySource
     */
    fun load(name: String, resource: String) : List<PropertySource<*>>
}