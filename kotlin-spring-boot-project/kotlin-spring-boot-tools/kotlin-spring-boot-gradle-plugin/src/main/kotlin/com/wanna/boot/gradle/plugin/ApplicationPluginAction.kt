package com.wanna.boot.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
open class ApplicationPluginAction : PluginApplicationAction {

    override fun getPluginClass(): Class<out Plugin<out Project>> = ApplicationPlugin::class.java
    override fun execute(project: Project) {

    }
}