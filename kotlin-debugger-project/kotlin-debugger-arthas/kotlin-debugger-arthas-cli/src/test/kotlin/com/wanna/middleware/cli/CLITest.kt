package com.wanna.middleware.cli

import com.wanna.middleware.cli.annotation.Argument
import com.wanna.middleware.cli.annotation.Name
import com.wanna.middleware.cli.annotation.Option

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
@Name("wanna")
class CLITest {

    @Option()
    fun setA(a: String?) {

    }

    @Argument(index = 0)
    fun setB(b: String?) {

    }

}

fun main() {
    val cli = CLIs.create(CLITest::class.java)
    println(cli)
}