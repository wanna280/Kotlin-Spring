package com.wanna.boot.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
open class KotlinPluginAction : PluginApplicationAction {

    override fun getPluginClass(): Class<out Plugin<out Project>> = KotlinPluginWrapper::class.java

    override fun execute(project: Project) {

    }
}