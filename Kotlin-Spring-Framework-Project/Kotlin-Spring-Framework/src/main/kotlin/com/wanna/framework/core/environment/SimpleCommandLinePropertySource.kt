package com.wanna.framework.core.environment

/**
 * 这是针对于命令行的PropertySource的一个简单实现，可以使用命令行参数的解析器，去将命令行参数列表解析成为一个CommandLineArgs对象；
 * 在CommandLineArgs对象当中存放了所以的nonOptionArgs以及optionArgs列表，可以通过getSource获取到CommandLineArgs对象
 *
 * @see CommandLinePropertySource
 * @see SimpleCommandLineArgsParser.parse
 * @see CommandLineArgs
 */
open class SimpleCommandLinePropertySource(vararg args: String) :
    CommandLinePropertySource<CommandLineArgs>(SimpleCommandLineArgsParser().parse(*args)) {

    companion object {
        const val COMMAND_LINE_PROPERTY_SOURCE_NAME = "commandLineArgs"
    }

    constructor(name: String, args: Array<String>) : this(*args) {
        this.name = name
    }

    override fun containsOption(name: String): Boolean {
        return source.containsOption(name)
    }

    override fun getOptionValues(name: String): List<String?>? {
        return source.getOptionValues(name)
    }

    override fun getNonOptionArgs(): List<String> {
        return source.getNonOptionArgs()
    }

    override fun getPropertyNames(): Array<String> {
        return this.source.getOptionNames().toTypedArray()
    }
}