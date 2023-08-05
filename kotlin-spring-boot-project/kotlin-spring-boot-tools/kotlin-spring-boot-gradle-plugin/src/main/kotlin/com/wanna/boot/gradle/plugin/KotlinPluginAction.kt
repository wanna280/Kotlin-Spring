package com.wanna.boot.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * 通过配置Kotlin.version属性来将用于Kotlin依赖关系管理的版本与其插件的版本对齐, 从而对Kotlin的Gradle插件做出反应
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 *
 * @see PluginApplicationAction
 */
open class KotlinPluginAction : PluginApplicationAction {

    companion object {
        private const val KOTLIN_VERSION = "kotlin.version"
    }


    override fun getPluginClass(): Class<out Plugin<out Project>> = KotlinPluginWrapper::class.java

    override fun execute(project: Project) {

        val extraProperties = project.extensions.extraProperties
        if (!extraProperties.has(KOTLIN_VERSION)) {
            // 对应Java的"KotlinPluginWrapperKt.getKotlinPluginVersion(project)", Kotlin当中可以使用扩展函数
            val kotlinPluginVersion = project.getKotlinPluginVersion()
            extraProperties.set(KOTLIN_VERSION, kotlinPluginVersion)
        }

        // 启用Kotlin编译器的JavaParameters参数
        enableJavaParametersOption(project)
    }

    /**
     * 启用Kotlin编译器的JavaParameters参数, 将所有的KotlinCompile的Task都去进行配置
     *
     * @param project Project
     */
    private fun enableJavaParametersOption(project: Project) {
        project.tasks.withType(KotlinCompile::class.java).configureEach {
            it.kotlinOptions.javaParameters = true
        }
    }
}