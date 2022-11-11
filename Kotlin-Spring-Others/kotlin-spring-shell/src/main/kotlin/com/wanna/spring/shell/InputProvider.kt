package com.wanna.spring.shell

/**
 * 输入的提供者，负责读取命令行当中的数据，并封装成为Input
 *
 * @see Input
 */
interface InputProvider {
    fun readInput(): Input
}