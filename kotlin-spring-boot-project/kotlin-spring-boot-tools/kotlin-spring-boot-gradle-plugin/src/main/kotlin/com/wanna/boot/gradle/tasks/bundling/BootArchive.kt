package com.wanna.boot.gradle.tasks.bundling

import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
interface BootArchive {

    /**
     * 获取应用的mainClass的全限定名
     *
     * @return mainClass
     */
    @Input
    fun getMainClass(): Property<String>

    /**
     * 获取将要包含在归档文件(Jar/War)当中的ClassPath
     *
     * @return classpath
     */
    @Optional
    @Classpath
    fun getClasspath(): FileCollection

    fun setClasspath(classpath: Any)

    fun setClasspath(classpath: FileCollection)
}