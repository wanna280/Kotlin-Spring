package com.wanna.framework.util

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.MimeType.Companion.valueOf
import java.io.Serializable
import java.nio.charset.Charset

/**
 * 代表了一个"MIME Type", 最开始是被定义在RFC2046当中, 并且后续也被用于一些别的网络传输协议(例如HTTP),
 * 对于媒体类型最早是应用于电子邮件(MIME)的, 因此媒体类型也被称为"MIME Type".
 *
 * 这个类当中并不包含用于去进行HTTP的内容协商的Q参数(q-parameter), 对于[com.wanna.framework.web.http.MediaType]
 * 这个类当中则提供了Q参数的支持, Q参数也就是qualityValue, 代表了该MediaType的权值, 从而用于去进行多个MediaType排序.
 *
 * [MimeType]类当中提供了[valueOf]方法去将"MIME Type"从字符串去解析成为[MimeType]对象, 对于更多工具方法, 可以参考[MimeTypeUtils]工具类.
 *
 * 对于一个MIME Type, 主要包含三个部分组成, type/subtype/parameters, 对于q-parameter和charset这些参数都是在parameters当中.
 * type称为大类, subtype称为小类, 目前大类一共有10个 ,常用的大类有下面六个: "application"/"text"/"image"/"video"/"multipart",
 * 对于type和subtype合并起来, 称为fullType(例如"application/json").
 *
 * "MIME Type"的结构: "application/json;k1=v1;k2=v2", 对于"application"也就是type, "json"也就是subtype,
 * 对于";"之后的内容: "k1=v1;k2=v2", 这部分代表的就是"MIME Type"的parameters. 对于"MIME Type"还会存在有结构后缀一说,
 * 例如"application/soap+xml"这样格式的"MIME Type", 就代表了W3C Web Service报文, 而对于报文的格式则采用"xml"的方式去进行承载.
 *
 * 对于"MIME Type"并不能唯一确定类型, 在网络协商(例如HTTP的内容协商)当中, "MIME Type"支持传递通配符, 例如"* / *"代表任意类型,
 * 例如"text / *"代表了文本大类, 因此对于使用"MIME Type"时, 不能简单去进行字符串的比较, 也应该考虑通配符的情况...
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/25
 *
 * @see MimeTypeUtils
 * @see com.wanna.framework.web.http.MediaType
 * @see com.wanna.framework.web.accept.ContentNegotiationManager
 *
 * @param type type大类型, 例如"text", "application"
 * @param subtype subtype子类型, 例如"html", "xml", "json"
 * @param parameters 更多的参数信息, 比如"quality", "charset"
 */
open class MimeType(val type: String, val subtype: String, val parameters: Map<String, String>) : Serializable {

    /**
     * type是否为通配符? (type为通配符的情况下, 其实subtype也为通配符, 不然不合法)
     */
    val isWildcardType: Boolean = this.type == WILDCARD_TYPE

    /**
     * subtype是否为通配符? (有可能有"*"和"*+"两者情况)
     */
    val isWildcardSubtype: Boolean = this.subtype == WILDCARD_TYPE || this.subtype.startsWith("*+")

    /**
     * MimeType是否是一个具体的? 只有type和subtype都不是通配符的情况下, 才算是一个具体的MimeType
     */
    val isConcrete: Boolean = !isWildcardType && !isWildcardSubtype

    /**
     * toString的结果的缓存, 避免多次重复生成
     */
    @Nullable
    private var toStringValue: String? = null

    /**
     * 已经解析完成的字符集的缓存
     */
    @Transient
    @Nullable
    var charset: Charset? = null
        private set

    init {
        // 执行参数的检查
        parameters.forEach(this::checkParameters)
    }

    /**
     * 根据MimeType的参数名, 去获取到参数信息
     *
     * @param name 参数名
     * @return 获取到的参数值(不存在该参数的话, return null)
     */
    @Nullable
    open fun getParameter(name: String): String? = parameters[name]

    /**
     * 检验参数是否合法?
     *
     * @param parameter parameter name
     * @param value value
     */
    protected open fun checkParameters(parameter: String, value: String) {
        // 如果这个参数是字符集的话, 先给存起来...
        if (PARAM_CHARSET == parameter) {
            if (this.charset == null) {
                this.charset = Charset.forName(value)
            }
        }
    }

    /**
     * 检查别的[MimeType]和当前的[MimeType]之间是否相互兼容?
     *
     * @param other 要去进行检查的别的[MimeType]
     * @return 如果两个[MimeType]之间相互兼容, return true; 否则return false
     */
    open fun isCompatibleWith(@Nullable other: MimeType?): Boolean {
        other ?: return false

        // 如果其中一个为"*/*"的话, 那么肯定是相互兼容的, return true
        if (this.isWildcardType || other.isWildcardType) {
            return true
        }

        // 如果两个的type都不是"*"的话, 那么需要先比对type, 再去比对subtype
        if (this.type == other.type) {
            // 如果type和subtype都完全相同, return true
            if (this.subtype == other.subtype) {
                return true
            }
            // 如果type相同, subtype不同的话... 只要subtype为"*"(或者"*+")才可能算是匹配的...
            if (this.isWildcardSubtype || other.isWildcardSubtype) {
                // 如果其中一个为"*", 那么它和另外一个MimeType一定是兼容的
                if (this.subtype == WILDCARD_TYPE || other.subtype == WILDCARD_TYPE) {
                    return true
                }
                // 如果两个subtype都不是"*"的话, 那么需要匹配一下"*+"的情况...

                // subtypeSuffix为"*+"之后的内容, 如果不还有"+", 那么值为null
                val thisSuffix = getSubtypeSuffix()
                val otherSuffix = other.getSubtypeSuffix()

                // 如果this含有"*+", 对于"application/*+xml"和"application/xml"这种是兼容的...
                if (this.isWildcardSubtype) {
                    return thisSuffix == otherSuffix || thisSuffix == other.subtype
                    // 如果other含有"*+"
                } else {
                    return thisSuffix == otherSuffix || otherSuffix == this.subtype
                }
            }
            // 如果两者subtype不同, 也都不为"*", return false
        }
        return false
    }

    /**
     * 检查当前[MimeType]是否包含别的[MimeType]?
     *
     * * 1."* / *"包含所有的[MimeType];
     * * 2."text / *"包含"text/html", 也包含"text/plain".
     *
     * @param other 别的[MimeType]
     * @return 如果当前[MimeType]包含other这个[MimeType], 那么return true; 否则return false
     */
    open fun includes(@Nullable other: MimeType?): Boolean {
        other ?: return false
        // 如果是"*/*", return true, 包含任何MimeType
        if (isWildcardType) {
            return true
        }

        // 如果两者type相同, 那么才需要去比较(type都不同的话, 一定不包含)
        if (type == other.type) {
            // 如果subtype也相同, 直接return true
            if (subtype == other.subtype) {
                return true
            }

            // 如果subtype当中含有"*"/"*+"的话, 那么还可能需要匹配一下
            if (this.isWildcardSubtype) {
                val thisPlusIndex = subtype.lastIndexOf('+')

                // 如果没有"+"的话, 那么说明subtype是"*", 直接return true
                if (thisPlusIndex == -1) {
                    return true
                }
                val otherPlusIndex = other.subtype.lastIndexOf('+')

                // 如果两者都有"+", 我们才需要去进行diff, 实际上只有类似"application/*+xml"包含"application/soap+xml"这一种情况
                // 此时需要比较"+"之后的内容是否完全相同, this的"+"之前是否是"*", 只有两种情况完全成立才算是包含...
                if (otherPlusIndex != -1) {
                    val thisSubtypeNoSuffix = subtype.substring(0, thisPlusIndex)

                    val thiSubtypeSuffix = subtype.substring(thisPlusIndex + 1)
                    val otherSubtypeSuffix = subtype.substring(otherPlusIndex + 1)
                    if (thiSubtypeSuffix == otherSubtypeSuffix && thisSubtypeNoSuffix == WILDCARD_TYPE) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * 当前[MimeType]是否比另外一个[MimeType]更加具体?
     *
     * @param other other MimeType
     * @return 如果当前更加具体return true; 否则return false
     */
    open fun isMoreSpecific(other: MimeType): Boolean {
        // 如果this是"*/*", 但是other不是"*/*", other更具体
        if (this.isWildcardType && !other.isWildcardType) {
            return false

            // 如果other是"*/*", 但是this不是"*/*", this更具体
        } else if (other.isWildcardType && !this.isWildcardType) {
            return true

        } else {
            // 如果this的subtype是"*", 但是other的subtype不是"*"的话, 那么other更具体
            if (this.isWildcardSubtype && !other.isWildcardSubtype) {
                return false

                // 如果other的subtype是"*", 但是this的subtype不是"*"的话, 那么this更具体
            } else if (other.isWildcardSubtype && !this.isWildcardSubtype) {
                return true

                // 如果两者的type和subtype完全一样, 那么按照参数数量去进行比较
            } else if (this.type == other.type && this.subtype == other.subtype) {
                val paramsSize1 = this.parameters.size
                val paramsSize2 = other.parameters.size
                return paramsSize1 - paramsSize2 > 0
            }
            return false
        }
    }

    /**
     * 检查另外一个[MimeType]是否比当前[MimeType]更加具体?
     *
     * @param other 别的[MimeType]
     * @return 如果别的更加具体return true; 否则return false
     */
    open fun isLessSpecific(other: MimeType): Boolean = other.isMoreSpecific(this)

    /**
     * 检查当前的[MimeType]是否包含在给定的[MimeType]的列表当中?
     * 只有在当前的[MimeType]对象和别的[MimeType]对象之间type和subtype都相等的情况下才算是匹配
     *
     * @param mimeTypes 待检查是否包含的MimeType列表
     * @return 如果当前MimeType包含在该列表当中, return true; 否则return false
     */
    open fun isPresentIn(@Nullable mimeTypes: Collection<MimeType>?): Boolean {
        mimeTypes ?: return false
        for (mimeType in mimeTypes) {
            if (equalsTypeAndSubtype(mimeType)) {
                return true
            }
        }
        return false
    }

    /**
     * 检查当前的[MimeType]和别的[MimeType]之间, 是否type和subtype之间都相等?
     *
     * @param other 别的MimeType
     * @return 如果两者type和subtype相同, return true; 否则return false
     */
    open fun equalsTypeAndSubtype(@Nullable other: MimeType?): Boolean {
        other ?: return false
        return this.type.equals(other.type, true)
                && this.subtype.equals(other.subtype, true)
    }

    /**
     * 获取获取到当前[MimeType]的结构后缀.
     *
     * 对于"application/soap+xml"这种情况, 需要返回subtype的后缀, 得到"xml";
     * 如果是"application/json"这种情况, 直接得到null, 因为该MimeType不含有结构后缀.
     *
     * @return subtype suffix
     */
    @Nullable
    open fun getSubtypeSuffix(): String? {
        val index = this.subtype.indexOf('+')
        if (index != -1 && this.subtype.length != index) {
            return this.subtype.substring(index + 1)
        }
        return null
    }

    /**
     * 将当前[MimeType]的toString的结果添加到给定的[builder]当中
     *
     * @param builder 待添加字符串的StringBuilder
     */
    protected open fun appendTo(builder: StringBuilder) {
        builder.append(this.type).append('/').append(this.subtype)
        for (parameter in parameters) {
            builder.append(';').append(parameter.key).append('=').append(parameter.value)
        }
    }

    /**
     * 将给定的字符串的引号去掉
     *
     * @param str str
     * @return 去掉引号之后的字符串
     */
    protected open fun unquote(str: String): String {
        return if (isQuotedString(str)) str.substring(1, str.length - 1) else str
    }

    /**
     * 检查给定的字符串是否被添加了引号, 从而导致被引号所包围? 可能是单引号/双引号
     *
     * @param str 待检查的字符串
     * @return 如果该字符串被引号包围, return true; 否则return false
     */
    private fun isQuotedString(str: String): Boolean {
        if (str.length < 2) {
            return false
        }
        return (str.startsWith('"') && str.endsWith('"'))
                || (str.startsWith('\'') && str.endsWith('\''))
    }


    /**
     * 生成toString的结果
     *
     * @return toString
     */
    override fun toString(): String {
        var value = toStringValue
        if (value == null) {
            val builder = StringBuilder()
            appendTo(builder)
            value = builder.toString()
            this.toStringValue = value
        }
        return value
    }

    /**
     * equals, 也采用type/subtype/parameters去进行生成
     *
     * @param other 要去进行比较的另外一个对象
     * @return 如果两者相等, return true; 否则return false
     */
    override fun equals(@Nullable other: Any?): Boolean {
        if (this === other) return true
        return other is MimeType && type == other.type && subtype == other.subtype && parametersAreEqual(other)
    }

    /**
     * 比较两者的参数是否都是相等的?
     *
     * @param other other
     * @return 如果两者参数相等, 那么return true; 否则return false
     */
    private fun parametersAreEqual(other: MimeType): Boolean {
        // shortcut, check size
        if (this.parameters.size != other.parameters.size) {
            return false
        }

        // check all entry equals
        for ((key, value) in parameters.entries) {
            if (!other.parameters.containsKey(key)) {
                return false
            }
            if (PARAM_CHARSET == key) {
                if (charset != other.charset) {
                    return false
                }
            } else if (value != other.parameters[key]) {
                return false
            }
        }
        return true
    }

    /**
     * hashCode, 采用type/subtype/parameters去进行生成
     *
     * @return hashCode
     */
    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + subtype.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }

    /**
     * 对[MimeType]的具体程度去进行排序的比较器
     */
    open class SpecificityComparator<T : MimeType> : Comparator<T> {

        /**
         * 返回-1代表mimeType1优先级高, 返回1代表mimeType2优先级高
         *
         * @param mimeType1 mimeType1
         * @param mimeType2 mimeType2
         */
        override fun compare(mimeType1: T, mimeType2: T): Int {
            // 如果mimeType1是"*/*", 但是mimeType2不是"*/*", 这种肯定mimeType2优先级更高
            if (mimeType1.isWildcardType && !mimeType2.isWildcardType) {
                return 1

                // 如果mimeType2是"*/*", 但是mimeType1不是"*/*", 这种肯定mimeType1优先级更高
            } else if (mimeType2.isWildcardType && !mimeType1.isWildcardType) {
                return -1

                // 如果mimeType1和mimeType2的type都不相同(例如"audio/basic" == "text/html", 特殊地, "*/*" == "*/*"), 那么没法比较, 认为两者相等
            } else if (mimeType2.type != mimeType1.type) {
                return 0

                // 如果mimeType1和mimeType2的type相同, 那么比较一下subtype
            } else {
                if (mimeType1.isWildcardSubtype && !mimeType2.isWildcardSubtype) {
                    return 1
                } else if (mimeType2.isWildcardSubtype && !mimeType1.isWildcardSubtype) {
                    return -1
                } else if (mimeType1.subtype != mimeType2.subtype) {
                    return 0
                }

                // 如果type和subtype都完全相同, 那么比较参数
                return compareParameters(mimeType1, mimeType2)
            }
        }

        /**
         * 在mimeType1和mimeType2的type和subtype都相同的情况下, 需要去比较参数,
         * 默认实现的比较方式为参数越多, 优先级越低.
         *
         * @param mimeType1 mimeType1
         * @param mimeType2 mimeType2
         * @return mimeType1和mimeType2, 两者按照参数去进行比较之后的结果
         */
        protected open fun compareParameters(mimeType1: T, mimeType2: T): Int {
            val paramsSize1 = mimeType1.parameters.size
            val paramsSize2 = mimeType2.parameters.size
            return paramsSize2.compareTo(paramsSize1)
        }
    }

    companion object {

        /**
         * 通配符, 可以用于"type"和"subtype"当中
         */
        const val WILDCARD_TYPE = "*"

        /**
         * MimeType的字符集的参数名, 可以通过这个参数去获取到字符集信息
         */
        const val PARAM_CHARSET = "charset"

        /**
         * 从mimeType的字符串, 去解析成为[MimeType]实例对象
         *
         * @param value 待解析成为MimeType的字符串
         * @return 解析得到的MimeType
         */
        @JvmStatic
        fun valueOf(value: String): MimeType {
            return MimeTypeUtils.parseMimeType(value)
        }
    }

}