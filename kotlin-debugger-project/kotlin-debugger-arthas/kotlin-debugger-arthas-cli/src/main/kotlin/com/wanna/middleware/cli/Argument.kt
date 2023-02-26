package com.wanna.middleware.cli

/**
 * 对于一个命令行来说, 分为name/option/argument三个部分
 *
 * 比如下面的命令当中:
 *
 * ```sh
 * java -jar xxx.jar
 * ```
 *
 * 其中, "java"是命令名, "-jar"为option(option一般以'-'或者是'--'作为开头), "xx.jar"为argument
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 *
 * @see CLI
 * @see Option
 */
open class Argument {

    private var index: Int = -1

    private var name: String = ""

    open fun setIndex(index: Int): Argument {
        this.index = index
        return this
    }

    open fun getIndex(): Int {
        return this.index
    }

    open fun setName(name: String): Argument {
        this.name = name
        return this
    }

    open fun getName(): String {
        return this.name
    }

}