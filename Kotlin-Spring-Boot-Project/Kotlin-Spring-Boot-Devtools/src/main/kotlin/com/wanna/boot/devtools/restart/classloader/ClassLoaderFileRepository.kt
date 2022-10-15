package com.wanna.boot.devtools.restart.classloader

import com.wanna.framework.lang.Nullable

/**
 * ClassLoaderFile的仓库，维护了许多的ClassLoaderFile
 *
 * @see ClassLoaderFile
 */
interface ClassLoaderFileRepository {
    companion object {
        // None的单例对象，不管获取什么file都return null
        @JvmField
        val NONE = object : ClassLoaderFileRepository {
            override fun getFile(name: String): ClassLoaderFile? = null
        }
    }

    /**
     * 根据name去获取指定的ClassLoaderFile
     *
     * @param name name
     * @return ClassLoaderFile(如果不存在return null)
     */
    @Nullable
    fun getFile(name: String): ClassLoaderFile?
}