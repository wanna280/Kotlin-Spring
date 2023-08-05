package com.wanna.boot.devtools.restart.classloader

import com.wanna.framework.lang.Nullable

/**
 * [ClassLoaderFile]的仓库, 内部维护了许多的[ClassLoaderFile]
 *
 * @see ClassLoaderFiles
 */
@FunctionalInterface
fun interface ClassLoaderFileRepository {
    companion object {
        /**
         * None的单例对象, 不管获取什么file都return null
         */
        @JvmField
        val NONE = ClassLoaderFileRepository { null }
    }

    /**
     * 根据文件名去获取对应的[ClassLoaderFile]
     *
     * @param name 文件名(例如"com/wanna/App.class")
     * @return ClassLoaderFile(如果不存在return null)
     */
    @Nullable
    fun getFile(name: String): ClassLoaderFile?
}