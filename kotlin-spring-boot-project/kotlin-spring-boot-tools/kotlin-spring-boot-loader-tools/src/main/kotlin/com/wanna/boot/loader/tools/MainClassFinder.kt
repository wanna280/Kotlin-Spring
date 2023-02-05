package com.wanna.boot.loader.tools

import java.io.File
import javax.annotation.Nullable

/**
 * SpringBoot的主类的寻找器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
object MainClassFinder {

    @Nullable
    @JvmStatic
    fun findSingleMainClass(rootDirectory: File, annotationName: String): String? {
        return null
    }
}