package com.wanna.boot.devtools.filewatch

import com.wanna.framework.lang.Nullable

/**
 * 用一个单例Bean的方式去实现SnapshotStateRepository
 *
 * @see SnapshotStateRepository
 */
object StaticSnapshotStateRepository : SnapshotStateRepository {

    /**
     * 保存的快照信息
     */
    @Volatile
    @Nullable
    private var state: Any? = null

    override fun store() = this.state

    override fun save(@Nullable state: Any?) {
        this.state = state
    }
}