package com.wanna.boot.gradle.plugin

import com.wanna.boot.gradle.dsl.SpringBootExtension
import com.wanna.boot.gradle.tasks.bundling.BootJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.*
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import java.util.concurrent.Callable

/**
 * 需要对JavaPlugin执行的操作的Action
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
open class JavaPluginAction : PluginApplicationAction {

    override fun getPluginClass(): Class<out Plugin<out Project>> = JavaPlugin::class.java

    override fun execute(project: Project) {
        // 配置ResolveMainClassName任务
        val resolveMainClassNameTask = configureResolveMainClassNameTask(project)

        // 配置BootJar任务
        configureBootJarTask(project, resolveMainClassNameTask)
    }

    /**
     * 给Project去配置一个[ResolveMainClassName]任务, 并将它去注册到Project的Tasks列表当中
     *
     * @param project Project
     * @return TaskProvider of ResolveMainClassName
     */
    private fun configureResolveMainClassNameTask(project: Project): TaskProvider<ResolveMainClassName> {
        return project.tasks.register(
            SpringBootPlugin.RESOLVE_MAIN_CLASS_NAME_TASK_NAME,
            ResolveMainClassName::class.java
        ) {

            // 设置BootJar任务的group(build/setup/verification/...), 对应的就是Intellij的Gradle插件的Tasks的一级目录...
            it.group = BasePlugin.BUILD_GROUP

            val classpath = Callable {
                project.extensions.getByType(SourceSetContainer::class.java)
                    .getByName(SourceSet.MAIN_SOURCE_SET_NAME).output
            }

            it.setClassPath(classpath)

            it.getConfiguredMainClassName().convention(project.provider {
                val javaApplicationMainClass = getJavaApplicationMainClass(project.extensions)
                if (javaApplicationMainClass != null) {
                    return@provider javaApplicationMainClass
                }

                val springBootExtension = project.extensions.getByType(SpringBootExtension::class.java)
                springBootExtension.mainClass.orNull
            })

            // 设置输出文件为"resolvedMainClassName"
            it.getOutputFile().set(project.layout.buildDirectory.file("resolvedMainClassName"))
        }
    }

    private fun getJavaApplicationMainClass(extensions: ExtensionContainer): String? {
        val javaApplication = extensions.findByType(JavaApplication::class.java) ?: return null
        return javaApplication.mainClass.orNull
    }

    /**
     * 给Project去配置一个[BootJar]任务, 并将它去注册到Project的Tasks当中去
     *
     * @param project Project
     * @param resolveMainClassName ResolveMainClassName Task
     */
    private fun configureBootJarTask(project: Project, resolveMainClassName: TaskProvider<ResolveMainClassName>) {
        // 获取main的SourceSet("src/main")
        val mainSourceSet =
            project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        // 根据MainSourceSet, 去计算得到ClassPath
        var classpath: Callable<FileCollection>
        try {
            // TODO, 这块暂时没做, 会有异常发生, 暂时走catch代码块
            val developmentOnly = project.configurations.getByName(SpringBootPlugin.DEVELOPMENT_ONLY_CONFIGURATION_NAME)
            val productionRuntimeClasspath =
                project.configurations.getByName(SpringBootPlugin.PRODUCTION_RUNTIME_CLASSPATH_CONFIGURATION_NAME)

            classpath = Callable {
                mainSourceSet.runtimeClasspath.minus(developmentOnly.minus(productionRuntimeClasspath))
                    .filter(JarTypeFileSpec())
            }
        } catch (ex: Exception) {
            classpath = Callable { mainSourceSet.runtimeClasspath.filter(JarTypeFileSpec()) }
        }


        // 给Project的Tasks当中去注册一个BootJar任务
        project.tasks.register(SpringBootPlugin.BOOT_JAR_TASK_NAME, BootJar::class.java) { bootJar ->
            // 设置BootJar任务的group(build/setup/verification/...), 对应的就是Intellij的Gradle插件的Tasks的一级目录...
            bootJar.group = BasePlugin.BUILD_GROUP

            // 从Manifest当中去解析"Start-Class"
            val manifestStartClass = project.provider { bootJar.manifest.attributes["Start-Class"] as String? }

            // 设置BootJar任务的ClassPath
            bootJar.setClasspath(classpath)

            // 如果Manifest当中有"Start-Class"的话, 那么直接使用; 不然的话, 就得去进行解析SpringBootApplication
            bootJar.getMainClass().convention(resolveMainClassName.flatMap {
                if (manifestStartClass.isPresent) manifestStartClass else resolveMainClassName.get().readMainClassName()
            })
        }

    }
}