package com.wanna.framework.util

import java.util.Properties

/**
 * 这是一个属性值的占位符的解析器的工具类，可以用它完成占位符的解析
 *
 * @see PlaceholderResolver
 */
open class PropertyPlaceholderHelper(
    private val prefix: String, private val suffix: String, private val valueSeparator: String? = null
) {
    // 简单前缀，如果是"%{}"则为"{"，如果是"%[]"则为"["，如果是"%()"则为"("
    private val simplePrefix = wellKnownSimplePrefix[suffix] ?: prefix

    companion object {
        // 已经知道的简单前缀
        private val wellKnownSimplePrefix = HashMap<String, String>(3)
        init {
            wellKnownSimplePrefix["}"] = "{"
            wellKnownSimplePrefix["]"] = "]"
            wellKnownSimplePrefix[")"] = "("
        }
    }

    /**
     * 给定一个Properties，从Properties当中去获取属性，去完成最终的占位符解析
     *
     * @param text 要去进行解析的目标占位符文本
     * @param properties 要解析的占位符的属性来源
     * @return 解析完成的占位符
     * @see parseStringValue
     */
    open fun replacePlaceholder(text: String, properties: Properties): String {
        return replacePlaceholder(text, properties::getProperty)
    }

    /**
     * 解析占位符，支持"%{%{user.name}} %{user.id}"这种情况，user.name等具体的属性值，甚至还可以是占位符，也支持去进行解析
     *
     * @param text 要去进行解析的目标占位符文本
     * @param placeholderResolver 占位符解析的策略接口，从哪获取属性的回调方法？
     * @return 解析完成的占位符
     * @see parseStringValue
     */
    open fun replacePlaceholder(text: String, placeholderResolver: PlaceholderResolver): String {
        return parseStringValue(text, placeholderResolver, LinkedHashSet())
    }

    /**
     * 解析占位符，本来需要传入的是一个PlaceholderResolver，为了能直接提供一个Kotlin函数引用，在这里使用适配器模式去做一层适配
     *
     * @param text 要去进行解析的目标占位符文本
     * @param placeholderResolver 占位符解析的策略接口，从哪获取属性的回调方法？
     * @return 解析完成的占位符
     * @see parseStringValue
     */
    open fun replacePlaceholder(text: String, placeholderResolver: (String) -> String?): String {
        return replacePlaceholder(text, object : PlaceholderResolver {
            override fun resolvePlaceholder(text: String) = placeholderResolver.invoke(text)
        })
    }

    /**
     * 解析字符串的值，在这里完成真正的占位符解析
     * @param text 要去进行解析的目标占位符文本
     * @param placeholderResolver 占位符解析的策略接口，从哪获取属性的回调方法？
     * @param visitedPlaceholder 已经完成解析的占位符，避免递归过程中，出现循环解析的情况...
     * @return 解析完成的占位符
     */
    protected open fun parseStringValue(
        text: String,
        placeholderResolver: PlaceholderResolver,
        visitedPlaceholder: MutableSet<String>
    ): String {
        var startIndex = text.indexOf(prefix)
        // 如果当前的字符串当中已经没有了占位符了，直接return 即可
        if (startIndex == -1) {
            return text
        }
        val builder = StringBuilder(text)  // 将text转为StringBuilder方便去进行操作
        while (startIndex != -1) {

            // 找到占位符的结束符(右括号)的位置index
            val endIndex = findPlaceholderEndIndex(builder, startIndex)
            if (endIndex != -1) {
                // [startIndex...endIndex+suffixLength]为真正的占位符的部分(包含前后缀)，[startIndex+prefixLength...endIndex]为占位符中间的属性值部分(不含前后缀)
                var placeholder: String? = builder.substring(startIndex + prefix.length, endIndex)
                val originalPlaceholder = placeholder!!

                // 如果add返回了false，说明本次解析的过程当中，出现了循环解析的情况...
                if (!visitedPlaceholder.add(originalPlaceholder)) {
                    throw IllegalArgumentException("解析占位符的过程当中，出现了循环解析的情况")
                }

                // 递归解析占位符
                placeholder = parseStringValue(placeholder, placeholderResolver, visitedPlaceholder)

                // 在拿到最终的属性值的key(比如user.name)之后，应该做的是解析key对应的value，这是就需要使用到placeholderResolver了
                var propertyValue = placeholderResolver.resolvePlaceholder(placeholder)

                // 如果属性值为空，但是有值分割符
                if (propertyValue == null && this.valueSeparator != null) {
                    val separatorIndex = placeholder.indexOf(this.valueSeparator)
                    // 获取到分割符的index
                    if (separatorIndex != -1) {
                        val actualPropertyKey = placeholder.substring(0, separatorIndex)  // 获取真实的propertyKey
                        val defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length)  // 默认值
                        // 如果能解析到propertyValue的话，那么使用propertyValue；不然使用defaultValue作为propertyValue
                        propertyValue = placeholderResolver.resolvePlaceholder(actualPropertyKey) ?: defaultValue
                    }
                }

                // 如果解析出来了值，比如user.name，但是它有可能值还是"%{}"的占位符的情况，因此还需要递归去进行解析和处理
                if (propertyValue != null) {
                    // 递归去进行解析
                    propertyValue = parseStringValue(propertyValue, placeholderResolver, visitedPlaceholder)
                    // 将字符串的"%{...}"部分，去替换成为解析完成的属性值(propertyValue)...
                    builder.replace(startIndex, endIndex + suffix.length, propertyValue)

                    // 重新计算startIndex，找到后续的下一个占位符
                    // 因为有可能出现"%{...} %{...}"这种情况，需要向后去继续解析后面的占位符
                    // 如果没有后面的占位符了，那么startIndex=-1，直接break掉跳出循环了
                    startIndex = builder.indexOf(prefix, startIndex + propertyValue.length)

                    // 如果解析到空，应该pass掉，解析后一个占位符，后一个占位符的位置是endIndex+suffixLength
                } else {
                    startIndex = builder.indexOf(prefix, endIndex + suffix.length)
                }
                visitedPlaceholder.remove(originalPlaceholder)  // remove掉
                // 如果已经没有找到了占位符的结束符，直接break掉
            } else {
                startIndex = -1
            }
        }
        return builder.toString()  // 返回解析占位符的结果
    }

    /**
     * 寻找占位符的结束(suffix)index，
     * 有可能会出现"%{%{%{user.name}}}"这种情况，使用withinNestedPlaceholder去完成计数
     * 在每次遇到前缀时，withinNestedPlaceholder++，计算前缀的出现次数；
     * 如果遇到一次后缀，那么就将withinNestedPlaceholder--；
     * 如果最终，withinNestedPlaceholder==0时，说明内部的全部占位符都解析完了，直接return，后面部分的字符串就不用管了
     *
     * @param text 要去寻找的文本
     * @param startIndex 从哪里开始寻找？
     * @return 找到的占位符的结束位置(如果没有找到，return -1)
     */
    protected open fun findPlaceholderEndIndex(text: CharSequence, startIndex: Int): Int {
        var index = startIndex + prefix.length  // 跳过startIndex之前的部分
        var withinNestedPlaceholder = 0

        while (index < text.length) {
            // 如果匹配suffix的话
            if (StringUtils.substringMatch(text, index, suffix)) {
                // 如果内部占位符的层数大于0的话，那么遇到后缀之后，可以抵消掉一层
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--
                    index += suffix.length

                    // 如果当前是suffix，并且内部的占位符的层数已经为0的话，那么说明匹配完成了，当前位置就是要找到的结束位置
                } else {
                    return index
                }
                // 如果匹配simplePrefix的话，那么内部的占位符层数++
            } else if (StringUtils.substringMatch(text, index, simplePrefix)) {
                withinNestedPlaceholder++
                index += simplePrefix.length
            } else {
                index++
            }
        }

        return -1
    }

    /**
     * 这是一个策略接口，它是一个占位符的解析器Resolver，完成属性值的获取，通过key去获取value的方式；
     * 供外部为占位符的解析去提供属性的来源的回调方法
     */
    interface PlaceholderResolver {
        fun resolvePlaceholder(text: String): String?
    }

}