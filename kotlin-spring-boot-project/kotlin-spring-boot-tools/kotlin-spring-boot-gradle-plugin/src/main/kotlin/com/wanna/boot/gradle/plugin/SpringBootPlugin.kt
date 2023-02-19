package com.wanna.boot.gradle.plugin

import com.wanna.boot.gradle.dsl.SpringBootExtension
import com.wanna.boot.gradle.util.VersionExtractor
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.function.Consumer

/**
 * SpringBoot Gradle Plugin的主入口
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
open class SpringBootPlugin : Plugin<Project> {

    companion object {
        /**
         * SpringBoot的版本
         */
        @JvmField
        val SPRING_BOOT_VERSION = VersionExtractor.forClass(DependencyManagementPluginAction::class.java)

        /**
         * BootJar的Gradle任务名
         */
        const val BOOT_JAR_TASK_NAME = "bootJar"

        /**
         * 解析SpringBoot的主类的Gradle任务名
         */
        const val RESOLVE_MAIN_CLASS_NAME_TASK_NAME = "resolveMainClassName"

        /**
         * productionRuntimeClasspath配置名
         */
        const val PRODUCTION_RUNTIME_CLASSPATH_CONFIGURATION_NAME = "productionRuntimeClasspath"

        /**
         * developmentOnly的配置名
         */
        const val DEVELOPMENT_ONLY_CONFIGURATION_NAME = "developmentOnly"
    }

    override fun apply(project: Project) {
        createExtension(project)
        registerPluginActions(project)
    }

    private fun createExtension(project: Project) {
        project.extensions.create("kotlinSpringBoot", SpringBootExtension::class.java, project)
    }

    /**
     * 为插件去注册Action
     *
     * @param project Project
     */
    private fun registerPluginActions(project: Project) {
        val pluginActions = listOf(
            JavaPluginAction(),
            WarPluginAction(),
            ApplicationPluginAction(),
            KotlinPluginAction()
        )

        // 将所有的插件Action, 去注册到Project的Plugins当中...
        for (pluginAction in pluginActions) {
            withPluginClassOfAction(pluginAction) { pluginClass ->
                project.plugins.withType(pluginClass) { pluginAction.execute(project) }
            }
        }
    }

    private fun withPluginClassOfAction(
        action: PluginApplicationAction,
        consumer: Consumer<Class<out Plugin<out Project>>>
    ) {
        try {
            consumer.accept(action.getPluginClass())
        } catch (ignore: Exception) {
            // ignore
        }
    }
}