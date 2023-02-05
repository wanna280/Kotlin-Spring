package com.wanna.boot.gradle.tasks.bundling

import org.apache.tools.ant.taskdefs.War
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
open class BootWar : War(), BootArchive {

    override fun getMainClass(): Property<String> {
        TODO("Not yet implemented")
    }

    override fun getClasspath(): FileCollection {
        TODO("Not yet implemented")
    }

    override fun setClasspath(classpath: Any) {
        TODO("Not yet implemented")
    }

    override fun setClasspath(classpath: FileCollection) {
        TODO("Not yet implemented")
    }
}