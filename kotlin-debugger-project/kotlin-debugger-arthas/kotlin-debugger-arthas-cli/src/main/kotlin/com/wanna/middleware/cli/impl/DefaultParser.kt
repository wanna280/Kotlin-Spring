package com.wanna.middleware.cli.impl

import com.wanna.middleware.cli.*
import javax.annotation.Nullable

/**
 * 命令行参数的解析器, 将字符串的参数列表, 去解析成为一个[CommandLine]对象
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
open class DefaultParser {

    private var cli: CLI? = null

    var expectedOptions: MutableList<Option>? = null

    var token: String? = null

    private var skipParsing: Boolean = false

    private var commandLine: DefaultCommandLine? = null

    /**
     * 当前正在处理的Option, 目的是为了收集Option后面的参数信息
     */
    private var current: Option? = null

    /**
     * 是否大小写敏感? 为true代表区分大小写, 为false代表不区分大小写
     */
    var caseSensitive: Boolean = true

    /**
     * 将给定的命令行参数列表, 去解析成为[CommandLine]对象
     *
     * @param cli CLI
     * @param commandLineArgs 命令行参数列表
     * @param validate 是否需要进行参数检验?
     */
    fun parse(cli: CLI, commandLineArgs: List<String>, validate: Boolean): CommandLine {
        this.cli = cli

        this.commandLine = CommandLines.create(cli)

        // 为所有的Argument, 去填充index
        var current = 0
        for (argument in cli.getArguments()) {
            if (argument.getIndex() == -1) {
                argument.setIndex(current)
                current++
            } else {
                current = argument.getIndex() + 1
            }
        }
        // sort arguments
        cli.setArguments(cli.getArguments().sortedWith(ArgumentComparator))

        // check Option
        for (option in cli.getOptions()) {
            option.ensureValidity()
        }

        // check Argument
        for (argument in cli.getArguments()) {
            argument.ensureValidity()
        }

        // 统计所有的必须Option
        this.expectedOptions = getRequiredOptions()

        // 解析命令行参数列表, 尝试对所有的参数去进行解析
        for (token in commandLineArgs) {
            visit(token)
        }

        return getCommandLine()
    }

    /**
     * 获取CLI当中全部的必须的Option选项
     *
     * @return required Options
     */
    private fun getRequiredOptions(): MutableList<Option> {
        val options = ArrayList<Option>()
        for (option in getCLI().getOptions()) {
            if (option.isRequired()) {
                options.add(option)
            }
        }
        return options
    }

    /**
     * 对单个Token字符串去进行解析
     *
     * @param token
     */
    private fun visit(token: String) {
        this.token = token

        // 如果上一个参数是"--"的话, 那么直接把当前token添加到Argument当中
        if (this.skipParsing) {
            handleArgument(token)
        } else if (token == "--") {
            this.skipParsing = true

            // 如果当前有正在进行处理的Option, 并且它还接收参数值,
            // 并且该token当中不含有longName/shortName的话, 那么将token收集到Option的参数当中器
        } else if (current != null && this.current!!.acceptValue() && this.isValue(token)) {
            getCommandLine().addRawValue(this.current!!, token)

            // 如果是"--"开头, 那么当作长参数(LongOption)去进行处理
            // 有可能是"--xxx=y"和"--xxx"这种形式
        } else if (token.startsWith("--")) {
            handleLongOption(token)

            // 如果是单纯的"-"开头, 那么当作短参数(ShortOption)去进行处理(当然, 遇到长参数也可以处理)
        } else if (token.startsWith("-") && token != "-") {
            handleShortAndLongOption(token)

            // 如果不是以"--"作为开头, 也不是以"-"作为开头, 那么当成普通的Argument去进行处理
        } else {
            handleArgument(token)
        }
    }

    /**
     * 处理LongOption的情况, 有可能是"--xxx=y"/"--xxx"两种形式
     *
     * * 1.对于"--xxx"这样的参数, 需要将current设置为对应的Option
     * * 2.对于"--xxx=y"这样的参数, 需要直接将K-V收集起来, 无需再去设置current
     *
     * @param token token
     */
    private fun handleLongOption(token: String) {
        if (token.indexOf('=') != -1) {
            handleLongOptionWithEquals(token)
        } else {
            handleLongOptionWithoutEquals(token)
        }
    }

    /**
     * 处理"--xxx=y"这样的格式的token参数
     *
     * @param token token
     */
    private fun handleLongOptionWithEquals(token: String) {
        val pos = token.indexOf('=')
        val opt = token.substring(0, pos)
        val value = token.substring(pos + 1)
        val matchingOptions = getMatchingOptions(opt)
        if (matchingOptions.isEmpty()) {
            handleArgument(token)
        } else {
            if (matchingOptions.size > 1) {
                throw IllegalStateException("cannot find unique option by token $opt")
            }

            val option = matchingOptions[0]
            if (!getCommandLine().acceptMoreValues(option)) {
                throw IllegalStateException("invalid value")
            }

            this.handleOption(option)
            getCommandLine().addRawValue(option, value)
            this.current = null
        }
    }

    /**
     * 处理"--xxx"这样的格式的token参数
     *
     * @param token token
     */
    private fun handleLongOptionWithoutEquals(token: String) {
        val matchingOptions = getMatchingOptions(token)
        if (matchingOptions.isEmpty()) {
            handleArgument(token)
        } else {
            if (matchingOptions.size > 1) {
                throw IllegalStateException("cannot find unique option by token $token")
            }
            val option = matchingOptions[0]
            this.handleOption(option)
        }
    }

    /**
     * 处理单个Option, 如果必要的话, 将它去设置成为当前正在处理的Option
     *
     * @param option Option
     */
    private fun handleOption(option: Option) {
        updateRequiredOptions(option)
        if (getCommandLine().acceptMoreValues(option)) {
            this.current = option
        } else {
            this.current = null
        }
    }

    private fun updateRequiredOptions(option: Option) {
        if (option.isRequired()) {
            expectedOptions?.remove(option)
        }
    }

    /**
     * 获取CLI当中的和longName匹配的Option
     *
     * @param token token
     * @return 和token对应的optionName匹配的Option列表
     */
    open fun getMatchingOptions(token: String): List<Option> {
        val opt = stripLeadingHyphens(token)

        for (option in getCLI().getOptions()) {
            if (this.optionEquals(option.getLongName(), opt)) {
                return listOf(option)
            }
        }

        val matchingOptions = ArrayList<Option>()
        // 如果找到最后, 都没有找到longName和opt相同的Option, 那么尝试一下基于前缀去进行匹配...
        for (option in getCLI().getOptions()) {
            if (option.getLongName() != Option.NO_NAME && option.getLongName().startsWith(opt)) {
                matchingOptions.add(option)
            }
        }
        return matchingOptions
    }

    /**
     * 去掉给定的字符串的前置的"-"字符
     *
     * @param str str
     * @return 去掉了前置的"-"字符之后的字符串
     */
    private fun stripLeadingHyphens(str: String): String {
        if (str.startsWith("--")) {
            return str.substring(2)
        } else if (str.startsWith("-")) {
            return str.substring(1)
        }
        return str
    }

    /**
     * 检查给定的两个optionName, 是否相同?
     *
     * @param name1 name1
     * @param name2 name2
     * @return 如果optionName相同, return true; 否则return false
     */
    private fun optionEquals(name1: String, name2: String): Boolean {
        return name1.equals(name2, !caseSensitive)
    }


    /**
     * 处理"-"开头的长参数/短参数
     *
     * @param token token
     */
    private fun handleShortAndLongOption(token: String) {
        val tokenToUse = stripLeadingHyphens(token)

        val index = tokenToUse.indexOf('=')

        // 如果长度为1, 那么尝试shortName的逻辑
        if (tokenToUse.length == 1) {
            if (hasOptionWithShortName(tokenToUse)) {
                handleOption(getOption(tokenToUse))
            } else {
                handleArgument(tokenToUse)
            }
        } else {

            // TODO

            // 如果token当中没有"="的话
            if (index == -1) {
                if (hasOptionWithShortName(tokenToUse)) {
                    handleOption(getOption(tokenToUse))
                } else if (this.getMatchingOptions(tokenToUse).isNotEmpty()) {
                    handleLongOptionWithoutEquals(tokenToUse)
                } else {

                }


                // 如果token当中有"="的话
            } else {
                val opt = tokenToUse.substring(0, index)
                val strip = tokenToUse.substring(index + 1)

                // 如果option长度为1, 那么尝试走shortOption
                if (opt.length == 1) {

                } else {

                }
            }
        }
    }

    /**
     * 根据name去寻找Option
     *
     * @param name name
     * @return Option
     */
    open fun getOption(name: String): Option {
        for (option in getCLI().getOptions()) {
            if (option.getLongName() == name || option.getShortName() == name) {
                return option
            }
        }
        throw IllegalStateException("cannot find Option by name $name")
    }

    private fun handleArgument(token: String) {
        getCommandLine().addArgumentValue(token)
    }

    /**
     * 判断给定的token, 是否是一个值? 对于shortName/longName, 都不算是一个值
     *
     * @param token token
     * @return 该token对应的是否是一个值?
     */
    private fun isValue(token: String): Boolean {
        return !isOption(token) || isNegativeNumber(token)
    }

    /**
     * 判断给定的token是否是一个Option?
     *
     * @param token token
     * @return 如果它是longOption/shortOption, 那么return true; 否则return false
     */
    private fun isOption(token: String): Boolean {
        return isLongOption(token) || isShortOption(token)
    }

    /**
     * 判断给定的token是否是一个Option
     *
     * @param token token
     * @return 如果该token是一个LongOption的话, return true; 负责return false
     */
    private fun isLongOption(token: String): Boolean {
        if (token.startsWith('-') && token.length != 1) {
            val index = token.indexOf('=')
            val str = if (index == -1) token else token.substring(index)

            // 如果可以寻找到合适的Option的话, 那么说明它是一个LongOption
            if (getMatchingOptions(str).isNotEmpty()) {
                return true
            } else {
                return getLongPrefix(token) != null && !token.startsWith("--")
            }
        }
        return false
    }

    /**
     * 获取LongOption的前缀, 根据给定的token, 切取任意长度的字符串, 去进行检验是否存在有合适的Option?
     *
     * @param token token
     * @return Option LongName Prefix(or null)
     */
    @Nullable
    private fun getLongPrefix(token: String): String? {
        val tokenToUse = stripLeadingHyphens(token)
        for (i in (2..tokenToUse.length - 2).reversed()) {
            val prefix = tokenToUse.substring(0, i)
            if (this.hasOptionWithLongName(tokenToUse)) {
                return prefix
            }
        }
        return null
    }

    /**
     * 检查CLI当中, 是否存在有和给定的name所匹配的longName的Option
     *
     * @param name name
     * @return 如果存在有name匹配的Option, return true; 否则return false
     */
    private fun hasOptionWithLongName(name: String): Boolean {
        for (option in getCLI().getOptions()) {
            if (optionEquals(option.getLongName(), name)) {
                return true
            }
        }
        return false
    }

    /**
     * 检查给定的token, 是否是一个Option的shortName
     *
     * @param token token
     * @return 如果该token对应的是一个Option的shortName, 那么return true; 否则return false
     */
    private fun isShortOption(token: String): Boolean {
        return token.startsWith('-') && token.length > 2 && hasOptionWithLongName(token)
    }

    /**
     * 检查是否存在有name, 和目标Option的shortName匹配的Option?
     *
     * @param name name
     * @return 如果存在有shortName匹配的name, 那么return true; 否则return false
     */
    private fun hasOptionWithShortName(name: String): Boolean {
        for (option in getCLI().getOptions()) {
            if (optionEquals(option.getShortName(), name)) {
                return true
            }
        }
        return false
    }

    private fun isNegativeNumber(token: String): Boolean {
        try {
            token.toDouble()
            return true
        } catch (ex: Throwable) {
            return false
        }
    }

    private fun getCLI(): CLI {
        return this.cli ?: throw IllegalStateException("CLI is not available")
    }

    private fun getCommandLine(): DefaultCommandLine {
        return this.commandLine ?: throw IllegalStateException("CommandLine is not available")
    }
}