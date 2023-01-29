package com.wanna.framework.util

import com.wanna.framework.lang.Nullable

/**
 * [MimeType]的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/25
 */
object MimeTypeUtils {

    /**
     * 比较两个[MimeType]之间谁更加具体的比较器
     */
    @JvmField
    val SPECIFICITY_COMPARATOR: Comparator<MimeType> = MimeType.SpecificityComparator()

    /**
     * 提供对于[MimeType]的LRU缓存, 将近期用到的MimeType的解析结果去进行缓存起来, 避免重复解析产生浪费,
     * 当从LRU缓存当中获取[MimeType]时, 如果缓存当中之前没有这样的[MimeType]的话, LRU缓存可以自动去构建出来[MimeType]对象
     */
    @JvmStatic
    private val cachedMimeTypes = ConcurrentLruCache(64, MimeTypeUtils::parseMimeTypeInternal)

    /**
     * 将给定的"mimeTypes"字符串, 例如"application/json, application/xml", 使用","去进行分割从而去解析成为[MimeType]列表
     *
     * @param mimeTypes mimeTypes字符串
     * @return 根据mimeTypes字符串去解析得到的[MimeType]列表
     */
    @JvmStatic
    fun parseMimeTypes(@Nullable mimeTypes: String?): List<MimeType> {
        if (!StringUtils.hasText(mimeTypes)) {
            return emptyList()
        }
        return tokenize(mimeTypes).filter(StringUtils::hasText).map(this::parseMimeType).toList()
    }

    /**
     * 将给定的[mimeType]字符串去解析成为MimeType
     *
     * @param mimeType 待解析的mimeType字符串
     * @return 解析得到的MimeType
     *
     * @throws InvalidMimeTypeException 如果解析MimeType的过程中遇到了不合法的情况
     */
    @Throws(InvalidMimeTypeException::class)
    @JvmStatic
    fun parseMimeType(@Nullable mimeType: String?): MimeType {
        if (!StringUtils.hasText(mimeType)) {
            throw InvalidMimeTypeException(mimeType ?: "null", "'mimeType' must not be empty")
        }
        // 对于multipart文件上传请求的MimeType, 不要走缓存, 需要去进行执行实时解析,
        // 因为在文件上传请求的MimeType的参数当中会存在有一个随机参数值(random boundaries), 需要去进行实时解析
        if (mimeType!!.startsWith("multipart")) {
            return parseMimeTypeInternal(mimeType)
        }
        // 对于正常的mimeType, 那么直接走缓存当中去进行获取
        return cachedMimeTypes[mimeType]
    }

    /**
     * 将给定的[mimeType]字符串去解析成为MimeType
     *
     * @param mimeType 待解析的mimeType字符串
     * @return 解析得到的MimeType
     * @throws InvalidMimeTypeException 如果解析MimeType的过程中遇到了不合法的情况
     */
    @Throws(InvalidMimeTypeException::class)
    @JvmStatic
    private fun parseMimeTypeInternal(mimeType: String): MimeType {
        // 切取";"前面的这部分去作为fullType(格式为type/subtype), 去掉前后多余的空白符
        var index = mimeType.indexOf(';')
        var fullType = (if (index != -1) mimeType.substring(0, index) else mimeType).trim()

        // 如果fullType为空...
        if (!StringUtils.hasText(fullType)) {
            throw InvalidMimeTypeException(mimeType, "'mimeType' must not be empty")
        }

        // java.net.HttpURLConnection有可能会返回一个"*; q=.2"这样的Accept header, 去转换一下
        if (fullType == MimeType.WILDCARD_TYPE) {
            fullType = "*/*"
        }
        val subIndex = fullType.indexOf('/')

        // 如果不含有"/", 那么不合法
        if (subIndex == -1) {
            throw InvalidMimeTypeException(mimeType, "does not contain '/'")
        }
        // 如果含有的"/"在最后一个字符, 那么说明根本就没有subtype, 不合法
        if (subIndex == fullType.length - 1) {
            throw InvalidMimeTypeException(mimeType, "does not contain subtype after '/'")
        }
        val type = fullType.substring(0, subIndex)
        val subtype = fullType.substring(subIndex + 1)

        // 如果type="*", 那么subtype必须为"*", 不允许出现"*/json"这种情况...
        if (type == MimeType.WILDCARD_TYPE && subtype != MimeType.WILDCARD_TYPE) {
            throw InvalidMimeTypeException(mimeType, "wildcard type is legal only in '*/*' (all mime types)")
        }

        // 解析出来type和subtype之后, 下面需要解析后面的参数...
        val parameters = LinkedHashMap<String, String>()

        do {
            // 使用nextIndex, 去实现往后搜索到下一个";"的位置...
            var nextIndex = index + 1
            var quoted = false
            while (nextIndex < mimeType.length) {
                val ch = mimeType[nextIndex]
                if (ch == ';') {
                    if (!quoted) {
                        break
                    }
                } else if (ch == '"') {
                    quoted = !quoted
                }
                nextIndex++
            }

            // 找到了下一个";"的话, 那么使用"="去切割key-value并添加到parameters当中
            val parameter = mimeType.substring(index + 1, nextIndex).trim()
            if (parameter.isNotBlank()) {
                val eqIndex = parameter.indexOf('=')
                if (eqIndex != -1) {
                    val key = parameter.substring(0, eqIndex)
                    val value = parameter.substring(eqIndex + 1)
                    parameters[key] = value
                }
            }
            index = nextIndex
        } while (index < mimeType.length)
        return MimeType(type, subtype, parameters)
    }

    /**
     * 将给定的字符串, 按照comma(",")去切割成为一个列表;
     * 它和普通的"tokenize"不同, 它也会将`"`(引号)也考虑进去, 并且`\`(右斜线)也将会被被去掉,
     *
     * 例如: 对于`"a,\"b\",c"`这样的一个字符串, 将会得到的结果是`[a, "b", c]`.
     *
     * @param mimeTypes 待进行切割的字符串
     * @return 切割之后得到的字符串列表
     */
    @JvmStatic
    fun tokenize(@Nullable mimeTypes: String?): List<String> {
        if (!StringUtils.hasText(mimeTypes)) {
            return emptyList()
        }
        val tokens = ArrayList<String>()
        var i = 0
        var startIndex = 0
        var inQuote = false
        while (i < mimeTypes!!.length) {
            when (mimeTypes[i]) {
                '"' -> inQuote = !inQuote
                ',' -> {
                    if (!inQuote) {
                        // 把引号之前的内容, 去切割出来
                        tokens += mimeTypes.substring(startIndex, i)
                        startIndex = i + 1
                    }
                }

                '\\' -> i++  // 去掉"\"
            }
            i++
        }
        tokens += mimeTypes.substring(startIndex)
        return tokens
    }

    /**
     * 将给定的[MimeType]列表当中的多个[MimeType]生成的字符串之间使用","去合成成为字符串,
     * 这个方法实现的想过, 本质上和[parseMimeTypes]方法之间是互逆操作
     *
     * @param mimeTypes 待转成字符串的[MimeType]列表
     * @return 将多个[MimeType]去聚合成为字符串的结果
     */
    @JvmStatic
    fun toString(mimeTypes: Collection<MimeType>): String {
        val builder = StringBuilder()
        for (mimeType in mimeTypes) {
            builder.append(mimeType.toString()).append(", ")
        }
        return builder.toString()
    }
}