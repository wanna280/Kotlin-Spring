package com.wanna.boot.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 针对指定的插件的Action操作
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
interface PluginApplicationAction : Action<Project> {

    /**
     * 获取到要去进行干预的插件
     *
     * @return 要去进行干预的插件类
     */
    fun getPluginClass(): Class<out Plugin<out Project>>
}