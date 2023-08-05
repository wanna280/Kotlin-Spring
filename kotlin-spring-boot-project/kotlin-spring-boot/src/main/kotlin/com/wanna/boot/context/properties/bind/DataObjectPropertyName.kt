package com.wanna.boot.context.properties.bind

import java.lang.StringBuilder

/**
 * DataObject的属性名的转换的工具类, 将属性名去转换成为使用'-'(dash)去进行连接的形式
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
object DataObjectPropertyName {

    /**
     * 将给定的属性名去转换成为dashed(破折号)形式的名字;
     * 例如: "userName"/"user-Name"/"user_Name"/"user_name"这些情况将会转换成为"user-name"
     *
     * @param name 原始的属性名
     * @return 转换成为dashed格式之后的属性名
     */
    @JvmStatic
    fun toDashedForm(name: String): String {
        val builder = StringBuilder(name.length)
        // 描述的是当前位置的字符, 是否是正在描述数组的索引?
        // 比如"[0]"这几个字符的inIndex=true, 在遇到'['会把inIndex标志位设置为true, 在遇到']'会把inIndex去设置为false
        var inIndex = false
        for (index in name.indices) {
            var ch = name[index]
            if (inIndex) {
                builder.append(ch)
                if (ch == ']') {
                    inIndex = false
                }
            } else {
                if (ch == '[') {
                    builder.append(ch)
                    inIndex = true
                } else {
                    // 如果当前符号是下划线的话, 转换成为破折号
                    ch = if (ch == '_') '-' else ch

                    // 如果当前字母是大写字母, 但是之前不存在有'-'的话, 那么得先补充个'-'
                    if (ch.isUpperCase() && index > 0 && builder.isNotEmpty() && builder[index - 1] != '-') {
                        builder.append('-')
                    }

                    // 把当前位置的字符串转换为小写, 加入到builder当中
                    builder.append(ch.lowercase())
                }
            }
        }
        return builder.toString()
    }
}