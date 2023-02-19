package com.wanna.boot.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.WarPlugin

/**
 * 需要对WarPlugin执行的操作的Action
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
open class WarPluginAction : PluginApplicationAction {

    override fun getPluginClass(): Class<out Plugin<out Project>> = WarPlugin::class.java

    override fun execute(t: Project) {
        TODO("Not yet implemented")
    }
}