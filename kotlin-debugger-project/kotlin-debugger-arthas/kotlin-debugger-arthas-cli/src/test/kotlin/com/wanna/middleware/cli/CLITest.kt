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

    @Option(longName = "pid")
    fun setPid(pid: Long?) {

    }

    @Option(longName = "core")
    fun setCore(core: Long) {

    }

    @Option(longName = "agent")
    fun setAgent(agent: String) {

    }

    @Argument(index = 0)
    fun setB(b: String?) {

    }

}

fun main() {
    val pidOption = TypedOption<Long>()
        .setType(Long::class.javaObjectType)
        .setShortName("pid")
        .setRequired(true)

    val coreOption = TypedOption<String>()
        .setType(String::class.java)
        .setShortName("core")
        .setRequired(true)

    val agentOption = TypedOption<String>()
        .setType(String::class.java)
        .setShortName("agent")
        .setRequired(true)

    val targetIpOption = TypedOption<String>()
        .setType(String::class.java)
        .setShortName("target-ip")

    val cli = CLIs.create(CLITest::class.java)
//        .addOption(pidOption)
//        .addOption(coreOption)
//        .addOption(agentOption)
//        .addOption(targetIpOption)

    val args = listOf<String>("--pid=1999", "--core", "1000", "--agent", "xxx")

    // 根据args去解析成为CommandLine
    val commandLine = cli.parse(args)

    val pid = commandLine.getOptionValue<Long>("pid")
    val core = commandLine.getOptionValue<Long>("core")
    val agent = commandLine.getOptionValue<String>("agent")





    println(cli)
}