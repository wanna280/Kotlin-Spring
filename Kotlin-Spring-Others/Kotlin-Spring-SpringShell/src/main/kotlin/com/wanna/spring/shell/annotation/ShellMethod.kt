package com.wanna.spring.shell.annotation

/**
 * ShellMethod
 *
 * @param key 支持去处理的commandName列表
 * @param value Shell命令的帮助信息
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ShellMethod(
    val key: Array<String> = [],
    val value: String = ""
)
