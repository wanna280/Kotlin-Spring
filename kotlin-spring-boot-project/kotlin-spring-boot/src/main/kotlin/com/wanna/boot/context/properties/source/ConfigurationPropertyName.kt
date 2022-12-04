package com.wanna.boot.context.properties.source

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.StringUtils
import java.util.function.Function
import kotlin.jvm.Throws
import kotlin.math.min

/**
 * 对一个配置的属性名去进行描述, 拆分成为一段一段的, 方便去进行描述,  比如"spring.config.name", 将会被拆分成为三段, ["spring", "config", "name"]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
class ConfigurationPropertyName(private val elements: Elements) {

    /**
     * 完成转换之后的每一级别的字符串, 比如"spring.config.name", 将会被拆分成为三段, ["spring", "config", "name"]
     */
    private val uniformed: Array<String> = Array(elements.size) { "" }


    /**
     * 属性名的字符串(toString之后的结果)
     */
    @Nullable
    private var string: String? = null

    /**
     * 给当前的[ConfigurationPropertyName]去添加一个后缀, 获取一个新的[ConfigurationPropertyName]
     *
     * @param suffix 添加的后缀
     * @return 带有后缀的新的[ConfigurationPropertyName]
     */
    fun append(suffix: String): ConfigurationPropertyName {
        // 如果给定的suffix没有字符, 那么return this即可, 不必merge
        if (!StringUtils.hasText(suffix)) {
            return this
        }
        // 新添加的suffix很可能只有一段元素(比如原始是"spring.main", 现在很可能就只是"type"/"name"之类的, 很少会遇到"xxx.yyy"这种情况)
        val additional = probablySingleElementOf(suffix)
        return ConfigurationPropertyName(this.elements.append(additional))
    }

    override fun toString(): String {
        if (this.string == null) {
            this.string = buildToString()
        }
        return this.string!!
    }

    private fun buildToString(): String {
        val elements = getNumberOfElements()
        val builder = StringBuilder(elements shl 3)
        for (index in 0 until elements) {
            val indexed = isIndexed(index)
            if (builder.isNotEmpty() && !indexed) {
                builder.append('.')
            }
            if (indexed) {
                builder.append('[').append(this.elements.get(index)).append(']')
            } else {
                builder.append(this.elements.get(index))
            }
        }
        return builder.toString()
    }

    /**
     * 获取当前的[ConfigurationPropertyName]的段的数量
     *
     * @return element size
     */
    fun getNumberOfElements(): Int = this.elements.size

    /**
     * 检查当前[ConfigurationPropertyName]的指定的段的类型是否是indexed? 也就是该段是否在'['和']'之间解析出来的
     *
     * @param index element index
     * @return 如果该段元素类型为INDEXED/NUMERICALLY_INDEXED, 那么return true; 否则return false
     */
    fun isIndexed(index: Int): Boolean = this.elements.getType(index).indexed

    /**
     * 检查给定的index对应的这一段Element的类型是否是在'[]'和']'之间, 并且值还是数字的? 比如"use.name[0]"当中"0"就是符合情况的
     *
     * @param index element index
     * @return 如果该段确实是以数字作为index, 那么return true; 否则return false
     */
    fun isNumbericIndex(index: Int): Boolean = this.elements.getType(index) == ElementType.NUMERICALLY_INDEXED

    /**
     * 根据size去切取指定长度的Element, 去构建出来一个新的[ConfigurationPropertyName], 相当于字符串的"substring(0,index)"操作
     *
     * @param size 要切取的Element的长度
     * @return 切片得到的新的[ConfigurationPropertyName]
     */
    fun chop(size: Int): ConfigurationPropertyName {
        if (size > getNumberOfElements()) {
            return this
        }
        return ConfigurationPropertyName(this.elements.chop(size))
    }

    /**
     * 根据index, 去获取到该位置的Element的字符串
     *
     * @param index element index
     * @return element index获取到的字符串
     */
    fun getElement(index: Int): String {
        return this.elements.get(index)
    }

    /**
     * 检查给定的Name, 是否是当前这个[ConfigurationPropertyName]的直接父属性?
     * 例如this="com.wanna.xxx.yyy".
     * * (1)name="com.wanna", return false;
     * * (2)name="com.wanna.xxx", return true
     *
     * @param name 需要去进行检查的元素
     * @return 如果是直接父属性的话, return true; 否则return false
     */
    fun isParentOf(name: ConfigurationPropertyName): Boolean {
        // 如果size的数量不匹配, 那么直接return false
        if (getNumberOfElements() != name.getNumberOfElements() - 1) {
            return false
        }
        return true
    }

    /**
     * 检查当前的[ConfigurationPropertyName]当中是否存在有属性名? 是否为空?
     *
     * @return 如果elements.size=0, 那么return true; 否则return false
     */
    fun isEmpty(): Boolean = this.elements.size == 0


    /**
     * 描述的是一个配置的属性名当中的元素, 并分为多段去进行描述
     *
     * Note: start和end配合使用, index对应的startIndex和endIndex就能对应到一段的元素的字符串;
     * 对于resolved(可以为null), 作用和start和end相同, 也可以根据index去去获取到该段元素的字符串.
     * 但是也可以两者一起使用, 给定一个index, 先从resolved当中去寻找, 找不到再根据start&end去进行寻找
     *
     * @param source 原始的字符串name
     * @param size 当前的Elements当中配置属性的段数
     * @param start startIndex数组
     * @param end endIndex数组
     * @param type 元素的类型数组
     * @param resolved 已经解析完成的resolved数组
     */
    class Elements(
        val source: String,
        val size: Int,
        val start: IntArray,
        val end: IntArray,
        val type: Array<ElementType>,
        @Nullable val resolved: Array<String>?
    ) {

        companion object {
            /**
             * 空的[Elements]常量
             */
            @JvmField
            val EMPTY = Elements("", 0, IntArray(0), IntArray(0), emptyArray(), null)
        }

        /**
         * 根据index获取该段元素类型
         *
         * @param index index
         * @return elementType
         */
        fun getType(index: Int): ElementType = type[index]

        /**
         * 根据size去进行切片, 相当于字符串的"substring(0, size)"
         *
         * @param size 要去进行切去的Element长度
         * @return 切取得到的Elements
         */
        fun chop(size: Int): Elements {
            // 根据size去计算得到新的resolved数组, 因为有可能需要取值的元素是在这里
            val newResolved = newResolved(size)
            // 把size去进行缩短成为给定的size...
            return Elements(this.source, size, this.start, this.end, this.type, newResolved)
        }

        /**
         * 获取解析完成的一段的属性值的name(先从resolved当中去进行寻找, 再根据startIndex&endIndex去进行寻找)
         *
         * @param index index
         * @return name
         */
        fun get(index: Int): String {
            if (this.resolved != null && index < resolved.size && this.resolved[index].isNotEmpty()) {
                return resolved[index]
            }
            return source.substring(this.start[index], this.end[index])
        }

        /**
         * 根据index去获取到该段元素的长度(先从resolved当中去进行寻找, 再根据startIndex&endIndex去进行寻找)
         *
         * @param index index
         * @return length
         */
        fun getLength(index: Int): Int {
            if (this.resolved != null && index < resolved.size && this.resolved[index].isNotEmpty()) {
                return resolved[index].length
            }
            return this.start[index] - this.end[index]
        }

        /**
         * 根据段index&段内部的charIndex去获取到对应的字符
         *
         * @param index 段index
         * @param charIndex 段元素当中的字符所在的index
         * @return char
         */
        fun charAt(index: Int, charIndex: Int): Char {
            return get(index)[charIndex]
        }

        /**
         * 将给定的[Elements]追加到当前的[Elements]之后, 得到一个新的[Elements]
         *
         * @param additional 需要去进行追加的[Elements]
         * @return 追加additional之得到的新的[Elements]
         */
        fun append(additional: Elements): Elements {
            val size = this.size + additional.size

            // 创建一个新的ElementType数组, 去进行merge当前的Elements和新的Elements当中的全部的ElementType
            val type = Array(size) { ElementType.EMPTY }
            System.arraycopy(this.type, 0, type, 0, this.size)
            System.arraycopy(additional.type, 0, type, this.size, additional.size)

            // 创建出来一个新的resolved数组, 如果之前就存在resolved的话, 把resolved拷贝过来(不存在就算了, 前面的元素全部都为空)
            val resolved = newResolved(size)

            // 把additional当中的那些元素去添加到resolved数组当中去...有可能前n段都是为空的, 但是此时会fallback, 去找start/end当中去进行寻找
            for (index in 0 until additional.size) {
                resolved[this.size + index] = additional.get(index)
            }

            // 这里的start和end, 直接沿用原来的就行了, 就不merge新的了...对于扩展出来的那部分元素, 能直接从resolved当中去进行获取
            return Elements(this.source, size, start, end, type, resolved)
        }

        /**
         * 创建一个新的resolved数组, 并将原始的resolved(如果存在的话)当中的内容拷贝过去
         *
         * @param size 预期的resolved数组的长度
         * @return 新的resolved数组
         */
        private fun newResolved(size: Int): Array<String> {
            val newResolved = Array(size) { "" }
            if (this.resolved != null) {
                System.arraycopy(this.resolved, 0, newResolved, 0, min(size, this.size))
            }
            return newResolved
        }

    }

    companion object {

        /**
         * 空属性Key的常量
         */
        @JvmField
        val EMPTY = ConfigurationPropertyName(Elements.EMPTY)

        /**
         * 根据name去创建[ConfigurationPropertyName]
         *
         * @param name 属性名
         * @return ConfigurationPropertyName
         * @throws InvalidConfigurationPropertyNameException 如果解析的过程当中遇到了不是小写字母/不是数字/不是'-'的情况
         */
        @JvmStatic
        @Throws(InvalidConfigurationPropertyNameException::class)
        fun of(name: String): ConfigurationPropertyName {
            return of(name, false)!!
        }

        /**
         * 需要添加的name很可能只有一段, 因此给定一个预期capacity为1的快捷构建方式;
         * 如果capacity=1不够用的话, 那么也支持去进行自动扩容
         *
         * @param name name
         * @return Elements
         */
        @JvmStatic
        private fun probablySingleElementOf(name: String): Elements {
            return elementsOf(name, false, 1)!!
        }

        /**
         * 根据属性名(name)去创建[ConfigurationPropertyName]
         *
         * @param name 属性名字符串
         * @param returnNullIfInvalid 如果不合法的话, 是否需要返回null?
         * @return 根据name属性名去构建出来的ConfigurationPropertyName
         */
        @Nullable
        @JvmStatic
        private fun of(name: String, returnNullIfInvalid: Boolean): ConfigurationPropertyName? {
            val elements = elementsOf(name, returnNullIfInvalid) ?: return null
            return ConfigurationPropertyName(elements)
        }

        /**
         * 根据初始的属性值字符串, 去解析得到[Elements], 把属性值当中的各个段去进行拆分
         *
         * @param name 原始的属性值字符串
         * @param returnNullIfInvalid 如果遇到不合法的情况, 是否return null? 如果设置为true, 不合法就会return null; 否则遇到不合法直接丢异常
         * @return 解析得到的[Elements], 如果returnNullIfInValid=false, 那么值一定非空; 如果returnNullIfInvalid=false, 那么不合法的情况下就会return null
         * @throws InvalidConfigurationPropertyNameException 如果解析的过程当中遇到了不是小写字母/不是数字/不是'-'的情况, 并且returnNullIfInValid=false
         */
        @Nullable
        @JvmStatic
        @Throws(InvalidConfigurationPropertyNameException::class)
        private fun elementsOf(name: String, returnNullIfInvalid: Boolean): Elements? {
            return elementsOf(name, returnNullIfInvalid, ElementsParser.DEFAULT_CAPACITY)
        }

        /**
         * 根据初始的属性值字符串, 去解析得到[Elements], 把属性值当中的各个段去进行拆分
         *
         * @param name 原始的属性值字符串
         * @param returnNullIfInvalid 如果遇到不合法的情况, 是否return null? 如果设置为true, 不合法就会return null; 否则遇到不合法直接丢异常
         * @param parserCapacity 初始的parser内部的start/end/type数组的容量
         * @return 解析得到的[Elements], 如果returnNullIfInValid=false, 那么值一定非空; 如果returnNullIfInvalid=false, 那么不合法的情况下就会return null
         * @throws InvalidConfigurationPropertyNameException 如果解析的过程当中遇到了不是小写字母/不是数字/不是'-'的情况, 并且returnNullIfInValid=false
         */
        @Nullable
        @JvmStatic
        @Throws(InvalidConfigurationPropertyNameException::class)
        private fun elementsOf(name: String, returnNullIfInvalid: Boolean, parserCapacity: Int): Elements? {
            // 如果为空的话, 那么return EMPTY
            if (name.isEmpty()) {
                return Elements.EMPTY
            }
            // 如果以"."作为开头, 或者以"."作为结尾, 那么都是不合法的...
            if (name[0] == '.' || name[name.length - 1] == '.') {
                if (returnNullIfInvalid) {
                    return null
                }
                throw InvalidConfigurationPropertyNameException(name, listOf('.'))
            }
            val elements = ElementsParser(name, '.', parserCapacity).parse()

            // 检查所有的Element, 是否存在有不合法的情况? 每个Element都只允许有小写字母/数字/'-'的情况
            for (index in 0 until elements.size) {
                val type = elements.getType(index)
                // 如果该段元素的类型是不合法的话, 那么需要return null或者是丢异常
                if (type == ElementType.NON_UNIFORM) {
                    if (returnNullIfInvalid) {
                        return null
                    }
                    // 丢出异常, 告诉这一段当中的某些字符的不合法
                    throw InvalidConfigurationPropertyNameException(name, getInvalidChars(elements, index))
                }
            }
            return elements
        }

        /**
         * 获取给定的[Elements]当中的index对应的段当中的所有的不合法的字符
         *
         * @param elements Elements
         * @param index 段index
         * @return 该段当中的所有的不合法的字符的列表
         */
        @JvmStatic
        private fun getInvalidChars(elements: Elements, index: Int): List<Char> {
            val invalidCharacters = ArrayList<Char>()
            for (charIndex in 0 until elements.get(index).length) {
                val ch = elements.charAt(index, charIndex)
                if (!ElementsParser.isValidChar(ch, charIndex)) {
                    invalidCharacters += ch
                }
            }
            return invalidCharacters
        }
    }

    /**
     * 提供将字符串去转换成为一个[Elements]的逻辑
     *
     * @param source 原始的属性名name, 比如"spring.main", 只能包含有小写字母/数字/'-'
     * @param separator 属性名多段的分隔符, 比如"spring.main"的分隔符为'.', 正常也就是'.'
     * @param capacity 初始的start/end/type的数组的空间大小(默认为6)
     */
    private class ElementsParser(
        private val source: String,
        private val separator: Char,
        private val capacity: Int = DEFAULT_CAPACITY
    ) {
        constructor(source: String, separator: Char) : this(source, separator, DEFAULT_CAPACITY)

        companion object {
            /**
             * 默认的容量
             */
            const val DEFAULT_CAPACITY = 6

            /**
             * 检查配置的属性名当中的字符是否合法?
             *
             * @param index 正在处理的字符的index
             * @param ch 正在处理的字符
             * @return 如果字符是一个小写字母/数字/'-'(但是不是'-'作为开头)的话, 都是合法的, return true; 否则return false
             */
            @JvmStatic
            fun isValidChar(ch: Char, index: Int): Boolean {
                return ch.isLowerCase() || ch.isDigit() || (index != 0 && ch == '-')
            }
        }

        /**
         * startIndex
         */
        private var start = IntArray(capacity)

        /**
         * endIndex
         */
        private var end = IntArray(capacity)

        /**
         * ElementType数组
         */
        private var type: Array<ElementType> = Array(capacity) { ElementType.EMPTY }

        /**
         * 解析完成的每一段的列表, 只有在指定了ValueProcessor的情况下, 才会存放到这里, 否则都只会记录到该段的起止位置并存放start和end当中
         */
        private var resolved: Array<String>? = null

        /**
         * 记录解析完成的Element的数量
         */
        private var size: Int = 0

        /**
         * 将source去解析成为Elements
         *
         * @return 解析得到的Elements
         */
        fun parse(): Elements = parse(null)

        /**
         * 将原始的属性名(source)去解析成为[Elements]
         *
         * @param valueProcessor 对当前这一段的值, 需要去进行自定义的处理之后才apply, 这个valueProcessor就可以去转换
         * @return 解析得到的[Elements]
         */
        fun parse(valueProcessor: Function<String, String>?): Elements {
            val length = this.source.length
            // 记录左括号的数量, 当遇到'['时+1, 当遇到']'时-1, 如果计算完整个属性名, 值不为0的话, 说明是不合法的
            var openBracketCount = 0

            // 记录当前正在处理的这段配置属性的起始位置, 初始化为0 会随着解析的过程, 不断向前移动
            var start = 0

            // 先把type初始化为EMPTY, 后面解析过程当中去进行慢慢更新
            var type = ElementType.EMPTY

            // 对
            source.indices.forEach {
                val ch = this.source[it]
                // 如果遇到了'[', 那么说明是遇到了数组index的前缀
                if (ch == '[') {
                    // 如果当前的括号数量为0的话, 那么就说明是之前的元素处理完了, 现在需要开始去处理index的情况了...
                    if (openBracketCount == 0) {

                        // 把'['括号之前的那一段的内容去收集起来
                        add(start, it, type, valueProcessor)

                        // 下一个元素的开始位置的位置是index+1, 也就是'['之后的字符;
                        // 将type暂时修改为NUMERICALLY_INDEXED, 当然其中不一定正确, 需要在后面去进行修正
                        // (如果是数字的话, 那么就是对的; 但是如果不是数字的话, 那么需要在后面去进行修正成为INDEXED)
                        start = it + 1
                        type = ElementType.NUMERICALLY_INDEXED
                    }
                    // 左括号的数量+1
                    openBracketCount++
                    // 如果遇到了']', 那么说明是遇到了数组index的后缀
                } else if (ch == ']') {
                    // 左括号的数量-1
                    openBracketCount--

                    // 如果当前的左括号数量为0的话, 那么说明数组的index的情况处理完了, 现在需要去处理括号之后的内容
                    if (openBracketCount == 0) {

                        // 把之前的'['和现在遇到的']'之间的这一段元素去收集起来
                        add(start, it, type, valueProcessor)

                        // 下一个元素的开始的位置是index+1, 也就是']'之后的那个字符; 将type先修改为EMPTY
                        start = it + 1
                        type = ElementType.EMPTY
                    }

                    // 如果当前正在处理的这一段内容, 不是数组的索引, 并且当前位置是'.'的话...
                } else if (!type.indexed && ch == separator) {

                    // 把刚刚遇到的这一段收集起来
                    add(start, it, type, valueProcessor)

                    // 下一个元素的开始位置是index+1, 也就是'.'之后的那个字符; 将type先修改为EMPTY
                    start = it + 1
                    type = ElementType.EMPTY

                    // 如果不是'[', 不是']', 也不是'.'的话, 那么就需要尝试去进行更新type
                } else {
                    type = updateType(type, ch, it - start)
                }
            }

            // 如果左括号的数量和右括号的数量不相同的话, 那么说明不合法...
            if (openBracketCount != 0) {
                type = ElementType.NON_UNIFORM
            }
            // 最后再去执行一遍add Element, 主要是修左右括号数量不匹配导致NON_UNIFORM的情况
            add(start, length, type, valueProcessor)

            return Elements(
                this.source, this.size, this.start,
                this.end, this.type, this.resolved
            )
        }

        /**
         * 根据当前字符的情况, 去更新正在处理的这一段的元素类型
         *
         * @param existingType 之前的元素类型
         * @param index 当前这个字符, 相对于当前这一段的字符的起始位置的偏移量
         * @param ch 正在处理的字符
         */
        private fun updateType(existingType: ElementType, ch: Char, index: Int): ElementType {
            // 如果正在处理的是'['和']'之间内容的话...有可能需要去修正type
            if (existingType.indexed) {
                // 如果原本是NUMERICALLY_INDEXED, 但是当前字符串并不是数字的话, 那么需要修正成为INDEXED
                if (existingType == ElementType.NUMERICALLY_INDEXED && !ch.isDigit()) {
                    return ElementType.INDEXED
                }
                return existingType
            }
            // 如果正在处理的不是'['和']'之间的内容的话...

            // 如果之前的元素类型还是未知, 并且当前的字符是小写字母/数字/'-', 那么就是合法的
            if (existingType == ElementType.EMPTY && isValidChar(ch, index)) {
                // 如果现在是这一段的第一个字母的话, 那么先去修改为UNIFORM
                // 如果现在不是这一段的第一个字母, 但是之前的type=EMPTY的话, 说明它是不合法的字符
                return if (index == 0) ElementType.UNIFORM else ElementType.NON_UNIFORM
            }

            // 如果之前的类型是正常的属性值, 但是现在遇到了'-'的话, 需要把类型改成DASHED
            if (existingType == ElementType.UNIFORM && ch == '-') {
                return ElementType.DASHED
            }

            // 来到这里, 有两种情况
            // 第一种可能: 之前是type=EMPTY, 但是第一个字符就是不合法的...比如大写字母A, 就会来到这里(return NON_UNIFORM)
            // 第二种可能: 前面的字符明明都是合法的, 如果这个字符不合法就return NON_UNIFORM; 如果这个字符合法就返回之前的type

            // 如果不是合法的数字/小写字母/'-'的话...
            if (!isValidChar(ch, index)) {
                // 如果之前的类型未知, 但是当前字母转小写之后, 还是不合法的话, 暂时用EMPTY
                if (existingType == ElementType.EMPTY && !isValidChar(ch.lowercaseChar(), index)) {
                    return ElementType.EMPTY
                }
                return ElementType.NON_UNIFORM
            }
            return existingType
        }

        /**
         * 将给定的[start,index)这一段的字符串元素, 去记录到start/end/type列表当中去, 最终需要转移到[Elements]当中去
         *
         * @param start startIndex
         * @param end endIndex
         * @param type elementType
         * @param valueProcessor 如果必要的话, 可以借助一个Function对于原始的字符串去进行自定义的转换
         */
        private fun add(start: Int, end: Int, type: ElementType, valueProcessor: Function<String, String>?) {
            // 如果区间长度小于, 或者type=EMPTY, 那么pass掉...
            if ((end - start <= 0) || type == ElementType.EMPTY) {
                return
            }
            // 如果之前的空间满了, 那么需要扩容
            if (this.size == capacity) {
                this.start = expand(this.start)
                this.end = expand(this.end)
                this.type = expand(this.type)
                this.resolved = expand(this.resolved)
            }

            var elementType: ElementType = type

            // 如果指定了ValueProcessor, 那么将会使用ValueProcessor去计算该段元素的值去进行计算, 并放入到resolved字段当中
            if (valueProcessor != null) {
                if (this.resolved == null) {
                    this.resolved = Array(this.start.size) { "" }
                }

                // 使用ValueProcessor, 使用substring(startIndex, endIndex)去进行切割得到这一段的元素的值, 并去进行转换
                val resolved = valueProcessor.apply(this.source.substring(start, end))
                val resolvedElements = ElementsParser(resolved, '.').parse()
                if (resolvedElements.size != 1) {
                    throw IllegalStateException("ResolvedElements不允许出现多个元素")
                }
                // 把当前解析得到的元素, 去加入到resolved列表最后一个元素...
                this.resolved!![this.size] = resolvedElements.get(0)
                elementType = resolvedElements.getType(0)
            }

            // 记录下来当前这一段的元素的start, end, elementType
            // 后续通过source.substring(start, end)即可获取到这一段的元素信息...
            this.start[this.size] = start
            this.end[this.size] = end
            this.type[this.size] = elementType

            // 处理完成之后size++
            this.size++
        }

        /**
         * 创建一个容量更大的IntArray, 并将src当中的内容拷贝到新的数组当中去
         *
         * @param src 原始的IntArray
         * @return 新的IntArray
         */
        private fun expand(src: IntArray): IntArray {
            val dest = IntArray(src.size + DEFAULT_CAPACITY)
            System.arraycopy(src, 0, dest, 0, src.size)
            return dest
        }

        /**
         * 创建一个容量更大的Element数组, 并将src当中的内容拷贝到新的数组当中去
         *
         * @param src 原始的Element数组
         * @return 新的Element数组
         */
        private fun expand(src: Array<ElementType>): Array<ElementType> {
            val dest = Array(src.size + DEFAULT_CAPACITY) { ElementType.EMPTY }
            System.arraycopy(src, 0, dest, 0, src.size)
            return dest
        }

        /**
         * 创建一个容量更大的String数组, 并将src当中的内容拷贝到新的数组当中去
         *
         * @param src 原始的String数组
         * @return 新的String数组
         */
        private fun expand(src: Array<String>?): Array<String>? {
            src ?: return null
            val dest = Array(src.size + DEFAULT_CAPACITY) { "" }
            System.arraycopy(src, 0, dest, 0, src.size)
            return dest
        }
    }

    /**
     * 描述的配置的属性名当中的一段的类型
     *
     * @param indexed 当前这个元素类型, 描述的是否是索引类型, 包括数组的index, 以及Map的Key的两种形式
     */
    enum class ElementType(val indexed: Boolean) {

        /**
         * 目前不知道是什么类型, 暂时设置为空类型
         */
        EMPTY(false),

        /**
         * 正常命名, 比如"username"
         */
        UNIFORM(false),

        /**
         * 破折号方式的命名, 比如"user-name"
         */
        DASHED(false),

        /**
         * 不合法的类型, 比如'['和']'的数量不一致的情况, 就是不合法的...
         */
        NON_UNIFORM(false),

        /**
         * 描述的是'['和']'之间的内容不是一个数字, 是一个字符串的情况, 这种情况是需要作为Map的Key去进行取值的
         */
        INDEXED(true),

        /**
         * 描述的是'['和']'之间的内容是一个数字的情况, 这种情况是需要去作为数组的Index去进行取值的
         */
        NUMERICALLY_INDEXED(true);

        fun allowsFastEqualityCheck(): Boolean = this == UNIFORM || this == NUMERICALLY_INDEXED

        fun allowsDashIgnoringEqualityCheck(): Boolean = this.allowsFastEqualityCheck() || this == DASHED
    }
}