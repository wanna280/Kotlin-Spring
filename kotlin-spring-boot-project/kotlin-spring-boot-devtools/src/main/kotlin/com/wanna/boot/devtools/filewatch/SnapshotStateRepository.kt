package com.wanna.boot.devtools.filewatch

/**
 * 被FileSystemWatcher用来去存储文件/文件夹的快照信息的仓库
 */
interface SnapshotStateRepository {
    companion object {
        // 单例Bean去进行维护的Repository
        val STATIC = StaticSnapshotStateRepository

        // 不去进行存储的Repository
        val NONE = object : SnapshotStateRepository {
            override fun store(): Any? {
                return null
            }

            override fun save(state: Any) {

            }
        }
    }


    /**
     * 获取之前存储的状态信息
     *
     * @return 之前存储的状态信息(如果之前没有存储的话, return null)
     */
    fun store(): Any?

    /**
     * 保存快照信息
     *
     * @param state state
     */
    fun save(state: Any)
}