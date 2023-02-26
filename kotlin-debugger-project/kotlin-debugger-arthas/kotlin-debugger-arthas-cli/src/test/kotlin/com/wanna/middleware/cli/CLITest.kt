package com.wanna.middleware.cli

import com.wanna.middleware.cli.annotation.Name

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
@Name("wanna")
class CLITest {

}

fun main() {
    val cli = CLIs.create(CLITest::class.java)
    println(cli)
}