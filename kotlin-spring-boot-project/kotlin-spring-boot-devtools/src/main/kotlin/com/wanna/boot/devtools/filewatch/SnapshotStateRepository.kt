package com.wanna.boot.devtools.filewatch

import com.wanna.boot.devtools.filewatch.SnapshotStateRepository.Companion.NONE
import com.wanna.boot.devtools.filewatch.SnapshotStateRepository.Companion.STATIC
import com.wanna.framework.lang.Nullable

/**
 * 被FileSystemWatcher用来去存储文件/文件夹的快照信息的仓库
 *
 * @see NONE
 * @see STATIC
 */
interface SnapshotStateRepository {
    companion object {
        /**
         * 单例Bean去进行维护的Repository
         */
        @JvmStatic
        val STATIC = StaticSnapshotStateRepository

        /**
         * 不去进行存储的Repository
         */
        @JvmStatic
        val NONE = object : SnapshotStateRepository {

            @Nullable
            override fun store(): Any? = null

            override fun save(@Nullable state: Any?) {

            }
        }
    }


    /**
     * 获取之前存储的状态信息
     *
     * @return 之前存储的状态信息(如果之前没有存储的话, return null)
     */
    @Nullable
    fun store(): Any?

    /**
     * 保存快照信息
     *
     * @param state 当前的快照信息
     */
    fun save(@Nullable state: Any?)
}