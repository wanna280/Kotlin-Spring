package com.wanna.boot.gradle.plugin

import org.gradle.api.specs.Spec
import java.io.File

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
open class JarTypeFileSpec : Spec<File> {

    override fun isSatisfiedBy(element: File): Boolean {
        return true
    }
}