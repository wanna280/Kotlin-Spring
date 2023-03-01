package com.wanna.middleware.cli.impl

import com.wanna.middleware.cli.*
import java.util.*
import javax.annotation.Nullable

/**
 * 默认的CLI命令行的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
open class DefaultCLI : CLI {

    private var name: String = ""

    private var hidden: Boolean = false

    private var summary: String = ""

    private var description: String = ""

    private var caseSensitive: Boolean = false

    private var options = ArrayList<Option>()

    private var arguments = ArrayList<Argument>()

    override fun parse(args: List<String>): CommandLine {
        return parse(args, true)
    }

    override fun parse(args: List<String>, validate: Boolean): CommandLine {
        val parser = DefaultParser()
        parser.caseSensitive = caseSensitive
        return parser.parse(this, args, validate)
    }

    override fun setName(name: String): CLI {
        this.name = name
        return this
    }

    override fun getName(): String {
        return this.name
    }

    override fun setDescription(description: String): CLI {
        this.description = description
        return this
    }

    override fun getDescription(): String {
        return this.description
    }

    override fun setSummary(summary: String): CLI {
        this.summary = summary
        return this
    }

    override fun getSummary(): String {
        return this.summary
    }

    override fun setHidden(hidden: Boolean): CLI {
        this.hidden = hidden
        return this
    }

    override fun isHidden(): Boolean {
        return this.hidden
    }

    override fun isCaseSensitive(): Boolean {
        return this.caseSensitive
    }

    override fun setCaseSensitive(caseSensitive: Boolean): CLI {
        this.caseSensitive = caseSensitive
        return this
    }

    override fun addOption(option: Option): CLI {
        this.options.add(option)
        return this
    }

    override fun addOptions(options: List<Option>): CLI {
        for (option in options) {
            this.options.add(option)
        }
        return this
    }

    override fun setOptions(options: List<Option>): CLI {
        this.options = ArrayList()
        return this.addOptions(options)
    }

    override fun removeOption(name: String): CLI {
        val iterator = options.iterator()
        while (iterator.hasNext()) {
            val option = iterator.next()
            if (optionOrArgumentEquals(option.getArgName(), name)) {
                iterator.remove()
            }
        }
        return this
    }

    @Nullable
    override fun getOption(name: String): Option? {
        for (option in options) {
            if (optionOrArgumentEquals(option.getArgName(), name)) {
                return option
            }
        }
        return null
    }

    override fun getOptions(): List<Option> {
        return this.options
    }

    override fun addArgument(argument: Argument): CLI {
        this.arguments.add(argument)
        return this
    }

    override fun addArguments(arguments: List<Argument>): CLI {
        for (argument in arguments) {
            this.arguments.add(argument)
        }
        return this
    }

    override fun setArguments(arguments: List<Argument>): CLI {
        this.arguments = ArrayList(arguments)
        return this
    }

    @Nullable
    override fun getArgument(index: Int): Argument? {
        if (index < 0) {
            throw IllegalArgumentException("Given index cannot be negative")
        }
        for (argument in arguments) {
            if (argument.getIndex() == index) {
                return argument
            }
        }
        return null
    }

    @Nullable
    override fun getArgument(name: String): Argument? {
        for (argument in arguments) {
            if (optionOrArgumentEquals(argument.getArgName(), name)) {
                return argument
            }
        }
        return null
    }

    override fun removeArgument(index: Int): CLI {
        for (argument in TreeSet(this.arguments)) {
            if (argument.getIndex() == index) {
                this.arguments.remove(argument)
            }
        }
        return this
    }

    override fun getArguments(): List<Argument> {
        return this.arguments
    }

    override fun usage(builder: StringBuilder): CLI {
        UsageMessageFormatter().usage(builder, null, this)
        return this
    }

    override fun usage(builder: StringBuilder, prefix: String): CLI {
        UsageMessageFormatter().usage(builder, prefix, this)
        return this
    }

    override fun usage(builder: StringBuilder, formatter: UsageMessageFormatter?): CLI {
        if (formatter == null) {
            UsageMessageFormatter().usage(builder, null, this)
        } else {
            formatter.usage(builder, null, this)
        }
        return this
    }

    /**
     * Option/Argument的参数名是否相同?
     *
     * @param name1 name1
     * @param name2 name2
     * @return 两个参数名相同return true; 否则return false
     */
    private fun optionOrArgumentEquals(name1: String, name2: String): Boolean {
        return name1.equals(name2, !caseSensitive)
    }
}