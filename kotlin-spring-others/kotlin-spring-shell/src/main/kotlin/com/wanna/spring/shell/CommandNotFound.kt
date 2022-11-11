package com.wanna.spring.shell

/**
 * 处理命令没有找到的情况
 *
 * @param words 命令当中的单词列表
 */
open class CommandNotFound(private val words: List<String>) : RuntimeException() {
    override val message: String
        get() = "Command Not Found (words='${words.joinToString(" ")}')"
}