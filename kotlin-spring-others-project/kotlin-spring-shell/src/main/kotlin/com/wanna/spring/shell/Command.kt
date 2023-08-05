package com.wanna.spring.shell

interface Command {

    /**
     * 该命令的帮助信息
     *
     * @param description 帮助信息
     * @param group 所在的group, 可以为null
     */
    open class Help(val description: String, val group: String? = null) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Help) return false

            if (description != other.description) return false
            if (group != other.group) return false

            return true
        }

        override fun hashCode(): Int {
            var result = description.hashCode()
            result = 31 * result + (group?.hashCode() ?: 0)
            return result
        }
    }
}