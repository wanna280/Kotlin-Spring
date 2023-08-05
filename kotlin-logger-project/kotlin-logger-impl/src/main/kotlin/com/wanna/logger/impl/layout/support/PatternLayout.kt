package com.wanna.logger.impl.layout.support

import com.wanna.logger.impl.event.ILoggingEvent
import com.wanna.logger.impl.event.LoggingEvent
import com.wanna.logger.impl.layout.LoggerLayout
import com.wanna.logger.impl.layout.converter.*
import java.util.HashMap

open class PatternLayout : LoggerLayout<ILoggingEvent> {

    /**
     * 格式化字符串-对应的Converter的映射关系
     */
    private val converterMap = HashMap<String, Class<out Converter<out ILoggingEvent>>>()

    private val converterCache = HashMap<Class<out Converter<out ILoggingEvent>>, Converter<ILoggingEvent>>()

    private var pattern: String? = null

    open fun setPattern(pattern: String) {
        this.pattern = pattern
    }

    open fun getPattern(): String? = this.pattern

    override fun doLayout(e: ILoggingEvent): String {
        val pattern = getPattern() ?: throw IllegalStateException("请先初始化Pattern")
        return format(e, pattern)
    }

    init {
        converterMap["t"] = ThreadNameConverter::class.java
        converterMap["thread"] = ThreadNameConverter::class.java

        converterMap["p"] = LoggerLevelConverter::class.java
        converterMap["le"] = LoggerLevelConverter::class.java
        converterMap["level"] = LoggerLevelConverter::class.java

        converterMap["d"] = DateConverter::class.java
        converterMap["C"] = LoggerNameConverter::class.java

        converterMap["msg"] = MessageConverter::class.java
        converterMap["message"] = MessageConverter::class.java
        converterMap["m"] = MessageConverter::class.java

        converterMap["n"] = ChangeLineConverter::class.java

        converterMap["green"] = ColorConverter::class.java
        converterMap["magenta"] = ColorConverter::class.java
        converterMap["red"] = ColorConverter::class.java
        converterMap["black"] = ColorConverter::class.java
        converterMap["blue"] = ColorConverter::class.java
    }

    /**
     * 将LoggingEvent按照指定的格式, 去格式格式化成为字符串, 去进行日志的输出
     *
     * @param event event
     * @param pattern 要匹配的模式
     * @return 转换得到的最终字符串, 用于去进行日志的输出
     */
    protected open fun format(event: ILoggingEvent, pattern: String): String {
        val builder = StringBuilder()
        var index = 0
        while (index < pattern.length) {
            if (pattern[index] == '%') {
                // 获取左括号、右括号以及空格的位置index(pattern[index..length]部分的字符串), 并利用data class去进行解构
                val (leftIndex, rightIndex, spaceIndex) = matchBracket(pattern, index)
                var patternToMatch = ""
                // 如果下一个遇到先是空格, 那么空格之前的部分有可能作为要进行解析的格式化字符串(比如%m,%n])
                // 但是有可能末尾会存在有用来进行美观的输出的反括号, 因此需要从最长的字符串一步步缩短长度去进行匹配
                if (spaceIndex != -1) {
                    for (rIndex in (index + 1 until spaceIndex).reversed()) {
                        patternToMatch = pattern.substring(index + 1, rIndex + 1)
                        val clazz = converterMap[patternToMatch]
                        if (clazz != null) {
                            builder.append(getHandledValue(clazz, patternToMatch, event))  // 拼接Converter转换的值
                                .append(pattern, rIndex + 1, spaceIndex)  // 拼接后面部分Converter没用到的值
                            break
                        }
                    }
                    index = spaceIndex  // index跳转到空格的位置
                    // 如果同时有左括号和右括号的话, 那么应该将左括号和右括号的部分混合之前的格式化字符串, 去交给Converter去进行处理
                } else if (leftIndex != -1 && rightIndex != -1) {
                    patternToMatch = pattern.substring(index + 1, leftIndex)
                    val clazz = converterMap[patternToMatch]
                    if (clazz != null) {
                        // 括号部分也是要用来去进行匹配的的, 从右括号的部分切割, 并交给Converter去完成解析
                        val matchToUse = pattern.substring(index, rightIndex + 1)
                        builder.append(getHandledValue(clazz, matchToUse, event))  // 拼接Converter转换的值
                    }
                    index = rightIndex + 1  // index跳转到右括号之后的位置
                    // 如果只要左括号没有右括号的话, 那么pass掉, 直接丢不合法的参数异常
                } else {
                    throw IllegalArgumentException("格式化当中之后出现了只有左括号没有右括号的情况")
                }
            } else {
                builder.append(pattern[index])
                index++
            }
        }
        return builder.toString()
    }

    /**
     * 给定一个带格式化的字符串表达式, 交给Converter去进行处理
     *
     * @param clazz Converter的类型
     * @param expression 表达式
     * @param event event
     * @return 使用Converter将expression转换之后的结果(支持递归处理)
     */
    private fun getHandledValue(
        clazz: Class<out Converter<out ILoggingEvent>>, expression: String, event: ILoggingEvent
    ): String {
        var handledValue = invokeTargetConverter(clazz, expression, event)
        // 如果转换之后, 还有%, 那么需要递归去进行处理, 而不是忽略掉, 因为有可能有%rea(%m)这种情况
        if (handledValue.indexOf('%') != -1) {
            handledValue = format(event, handledValue)
        }
        return handledValue
    }

    /**
     * 执行目标Converter的convert方法, 获取Converter转换之后的结果
     *
     * @param clazz Converter类型
     * @param expression 表达式
     * @param event event
     * @return 经过Converter转换之后的字符串
     */
    @Suppress("UNCHECKED_CAST")
    private fun invokeTargetConverter(
        clazz: Class<out Converter<out ILoggingEvent>>, expression: String, event: ILoggingEvent
    ): String {
        var handler = converterCache[clazz]
        if (handler == null) {
            handler = clazz.getDeclaredConstructor().newInstance() as Converter<ILoggingEvent>
            converterCache[clazz] = handler
        }
        return handler.convert(expression, event)
    }


    /**
     * 从startIndex位置开始向前匹配到最前面的一个括号
     *
     * @param pattern 匹配的模式字符串
     * @param startIndex 开始匹配的index
     * @return 括号(左/右)的位置
     */
    private fun matchBracket(pattern: String, startIndex: Int): Bracket {
        val spaceIndex = pattern.indexOf(' ', startIndex)
        val leftIndex0 = pattern.indexOf('(', startIndex)
        val leftIndex1 = pattern.indexOf('[', startIndex)
        val leftIndex2 = pattern.indexOf('{', startIndex)

        // 找到三者之中最前面的一个括号的位置, 如果三个括号都不存在的话, leftIndex=Int.MAX_VALUE
        val leftIndex = minOf(
            if (leftIndex0 == -1) Int.MAX_VALUE else leftIndex0,
            if (leftIndex1 == -1) Int.MAX_VALUE else leftIndex1,
            if (leftIndex2 == -1) Int.MAX_VALUE else leftIndex2
        )
        // (1)如果后面根本没有括号, 那么leftIndex=rightIndex=-1
        // (2)如果先出现空格, 后出现括号, 那么以空格为准
        return if (leftIndex == Int.MAX_VALUE || spaceIndex < leftIndex) {
            Bracket(-1, -1, if (spaceIndex == -1) pattern.length else spaceIndex)
            //(3)如果先出现括号, 后出现空格的话, 需要匹配到右括号的位置...
        } else {
            Bracket(leftIndex, matchRightBracket(pattern, leftIndex), -1)
        }
    }

    /**
     * 根据左括号的类型, 去匹配到右括号的位置
     *
     * @param pattern 整个pattern字符串
     * @param currentIndex 当前左括号(,[,{位置的索引
     * @return 匹配到的右括号的index, 如果没有匹配到, return -1
     */
    private fun matchRightBracket(pattern: String, currentIndex: Int): Int {
        var rightIndex = -1
        if (currentIndex < pattern.length && currentIndex >= 0) {
            if (pattern[currentIndex] == '(') {
                rightIndex = pattern.indexOf(')', currentIndex + 1)
            } else if (pattern[currentIndex] == '[') {
                rightIndex = pattern.indexOf(']', currentIndex + 1)
            } else if (pattern[currentIndex] == '{') {
                rightIndex = pattern.indexOf('}', currentIndex + 1)
            }
        }
        return rightIndex
    }

    /**
     * 括号
     *
     * @param leftIndex 左括号的位置
     * @param rightIndex 右括号的位置
     * @param spaceIndex 空格的位置
     */
    data class Bracket(val leftIndex: Int, val rightIndex: Int, val spaceIndex: Int = -1)
}