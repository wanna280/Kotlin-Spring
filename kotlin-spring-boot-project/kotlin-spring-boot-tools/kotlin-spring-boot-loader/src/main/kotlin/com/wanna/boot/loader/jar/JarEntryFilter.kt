package com.wanna.boot.loader.jar

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/3
 */
interface JarEntryFilter {
    fun apply(name: AsciiBytes): AsciiBytes?
}