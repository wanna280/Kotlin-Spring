package com.wanna.middleware.cli

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
class CLITest {

}

fun main() {
    val cli = CLIs.create("wanna")
    cli.addOption(Option())
        .addArgument(Argument())
}