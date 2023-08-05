package com.wanna.spring.shell

interface Input {
    /**
     * 原始的文本
     *
     * @return command原始文本
     */
    fun rawText(): String

    /**
     * command当中的单词列表
     *
     * @return 命令当中的word列表
     */
    fun words(): List<String>
}