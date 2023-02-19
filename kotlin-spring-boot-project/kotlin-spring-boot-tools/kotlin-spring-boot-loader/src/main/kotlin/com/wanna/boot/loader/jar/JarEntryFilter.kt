package com.wanna.boot.loader.jar

import javax.annotation.Nullable

/**
 * 对于JarEntry提供过滤的Filter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/3
 */
fun interface JarEntryFilter {

    /**
     * 对JarEntry去执行过滤的过滤方法
     *
     * @param name entryName
     * @return 转换之后的entryName(or null)
     */
    @Nullable
    fun apply(name: AsciiBytes): AsciiBytes?
}