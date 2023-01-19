package com.wanna.framework.core.environment

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.StringUtils

/**
 * 这是命令行的PropertySource, 为所有的基于命令行的PropertySource提供一层抽象的规范;
 * 命令行参数有两类, 一类是"-xxx", 另外一类是"--xxx=yyy"这两类, 我们将第一类称为noOptionArgs, 第二类称为optionArgs;
 * 第一类参数没有key-value之分, 直接放在一起去进行存放; 第二类参数有key-value之分, 需要切割成为key-value去进行存放
 *
 * @see SimpleCommandLinePropertySource
 *
 * @param name PropertySourceName
 * @param source source
 */
abstract class CommandLinePropertySource<T>(name: String, source: T) : EnumerablePropertySource<T>(name, source) {

    companion object {
        /**
         * 命令行的PropertySource的默认name
         */
        const val COMMAND_LINE_PROPERTY_SOURCE_NAME = "commandLineArgs"

        /**
         *  无option参数的属性的默认PropertyName
         */
        const val DEFAULT_NO_OPTION_ARGS_PROPERTY_NAME = "noOptionArgs"
    }

    constructor(source: T) : this(COMMAND_LINE_PROPERTY_SOURCE_NAME, source)

    /**
     * 无option的参数的属性值的name
     */
    private var noOptionArgsPropertyName: String = DEFAULT_NO_OPTION_ARGS_PROPERTY_NAME

    /**
     * 根据属性名去进行获取属性值
     * (1)如果name为noOptionArgs的属性值name, 那么返回所有的noOptionArgs列表(使用","进行分割);
     * (2)如果name不为noOptionArgs的属性值name, 那么需要从列表当中获取到所有的值(并使用","去进行分割)
     *
     * @param name 参数名
     * @return 获取到的参数值(获取不到return null)
     */
    @Nullable
    override fun getProperty(name: String): Any? {
        if (noOptionArgsPropertyName == name) {
            val nonOptionArgs = getNonOptionArgs()
            return if (nonOptionArgs.isEmpty()) null
            else StringUtils.collectionToCommaDelimitedString(nonOptionArgs)

            // 如果name不是无Option类型的参数, 那么...
        } else {
            val optionValues: List<String?>? = getOptionValues(name)

            // 如果不存在有这样的属性值, 那么return null
            return if (optionValues == null) null

            // 如果存在的话, 那么使用","把它拼接成为字符串去进行返回
            else StringUtils.collectionToCommaDelimitedString(optionValues)
        }
    }

    /**
     * 在命令行的PropertySource当中是否存在有该属性值?
     * (1)如果name为noOptionArgs的属性值name, 那么需要判断noOptionArgs是否为空
     * (2)如果name不为noOptionArgs的属性值name, 那么就从optionArgs当中去进行判断
     */
    override fun containsProperty(name: String): Boolean {
        // 如果给定的是无option的propertyName, 那么需要判断无option的参数是否为空
        if (noOptionArgsPropertyName == name) {
            return this.getNonOptionArgs().isNotEmpty()
        }
        // 如果给定的不是无option的propertyName, 那么需要从有option的参数当中去进行判断
        return this.containsOption(name)
    }

    /**
     * PropertySource当中是否已经包含了该参数? 交给子类去进行实现
     *
     * @param name 参数名
     * @return 如果包含该参数, 那么return true; 否则return true
     */
    abstract fun containsOption(name: String): Boolean

    /**
     * 获取可选的参数值, 根据key去获取value列表, 交给子类去进行实现
     *
     * @param name 参数名
     * @return 获取到的参数值列表(如果不存在, return null)
     */
    @Nullable
    abstract fun getOptionValues(name: String): List<String?>?

    /**
     * 获取无参数名的参数列表, 具体逻辑, 交给子类去进行实现
     *
     * @return 无参数名的参数列表
     */
    abstract fun getNonOptionArgs(): List<String>
}