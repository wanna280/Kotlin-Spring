package com.wanna.boot.system

import javax.annotation.Nullable

/**
 * 应用所在的进程的PID
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/20
 */
open class ApplicationPid() {

    /**
     * PID
     */
    @Nullable
    private var pid: String? = getPid()

    protected constructor(pid: String) : this() {
        this.pid = pid
    }

    /**
     * 获取当前进程的PID
     *
     * @return 当前进程PID
     */
    @Nullable
    private fun getPid(): String? {
        try {
            return ProcessHandle.current().pid().toString()
        } catch (ex: Exception) {
            return null
        }
    }

    /**
     * toString
     *
     * @return toString
     */
    override fun toString(): String = this.pid ?: ""
    override fun equals(@Nullable other: Any?): Boolean {
        if (this === other) return true
        if (other is ApplicationPid) {
            return this.pid == other.pid
        }
        return false
    }

    override fun hashCode(): Int = pid?.hashCode() ?: 0
}