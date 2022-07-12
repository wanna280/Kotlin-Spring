package com.wanna.boot.devtools.filewatch

/**
 * 用一个单例Bean的方式去做SnapshotStateRepository
 *
 * @see SnapshotStateRepository
 */
object StaticSnapshotStateRepository : SnapshotStateRepository {

    private var state: Any? = null

    override fun store() = this.state

    override fun save(state: Any) {
        this.state = state
    }
}