package com.wanna.boot.gradle.dsl

import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * SpringBoot的Gradle DSL的入口点(EntryPoint)
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
open class SpringBootExtension(private val project: Project) {

    /**
     * 配置SpringBoot的主启动类的属性
     */
    val mainClass: Property<String> = project.objects.property(String::class.java)
}