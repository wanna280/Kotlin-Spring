package com.wanna.framework.web.http

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.MimeType
import com.wanna.framework.util.MimeTypeUtils
import com.wanna.framework.util.StringUtils
import java.io.Serializable
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * HTTP协议当中需要用到的MediaType媒体类型, 它用于去决定浏览器将以什么形式、什么编码对资源进行解析;
 * 对于一个MediaType, 主要包含4个部分组成, type/subtype/charset/qualityValue,
 * 实际上, 对于charset和qualityValue这两个参数都是存放在parameters当中.
 *
 * @param type type, 例如"text", "application"
 * @param subtype subtype, 例如"html", "xml", "json"
 * @param parameters 更多的参数信息, 比如"quality", "charset"
 *
 * @see com.wanna.framework.web.accept.ContentNegotiationManager
 */
open class MediaType(type: String, subtype: String, parameters: Map<String, String>) :
    MimeType(type, subtype, parameters), Serializable {

    init {
        // 执行初始化, 主要用于从parameter当中去解析qualityValue和charset
        parameters.forEach { (name, value) -> checkParameters(name, value) }
    }

    /**
     * MediaType的权值
     */
    val qualityValue: Double
        get() {
            val qualityFactor = getParameter(PARAM_QUALITY_FACTOR)
            return if (qualityFactor != null) unquote(qualityFactor).toDouble() else 1.0
        }

    constructor(other: MediaType, parameters: Map<String, String>) : this(other.type, other.subtype, parameters)

    constructor(type: String, subtype: String) : this(type, subtype, emptyMap())

    constructor(type: String) : this(type, WILDCARD_TYPE)

    constructor(type: String, subtype: String, qualityValue: Double) : this(
        type, subtype, mapOf(PARAM_QUALITY_FACTOR to qualityValue.toString())
    )

    constructor(type: String, subtype: String, charset: Charset) : this(
        type,
        subtype,
        mapOf(PARAM_CHARSET to charset.name())
    )


    constructor(other: MediaType, charset: Charset) : this(other.type, other.subtype, charset)

    constructor(mimeType: MimeType) : this(mimeType.type, mimeType.subtype, mimeType.parameters)

    /**
     * 检查当前参数是否合法, 并完成初始化相关参数的初始化
     *
     * @param parameter 参数名
     * @param value 参数值
     */
    override fun checkParameters(parameter: String, value: String) {
        super.checkParameters(parameter, value)

        // 检查一下qualifyValue权值是否合法? 对于MediaType的权值只能在0-1之间
        if (PARAM_QUALITY_FACTOR == parameter) {
            val unquotedValue = unquote(value)
            val quality = unquotedValue.toDouble()
            if (quality !in 0.0..1.0) {
                throw IllegalStateException("Invalid quality value \"$unquotedValue\": should be between 0.0 and 1.0")
            }
        }
    }

    open fun includes(@Nullable other: MediaType?): Boolean {
        return super.includes(other)
    }

    /**
     * 检查当前的MediaType, 和另外一个MediaType之间两者是否兼容
     *
     * @param other other MediaType
     * @return 如果两者之间兼容的话, return true; 否则return false
     */
    open fun isCompatibleWith(@Nullable other: MediaType?): Boolean {
        return super.isCompatibleWith(other)
    }

    /**
     * 将给定的[MediaType]的qualityValue, 拷贝到当前的[MediaType]当中, 从而去实现合并, 得到新的[MediaType]
     *
     * @param mediaType 需要提供qualityValue的MediaType
     * @return 包含了this当中的type/subtype/charset信息, 以及别的MediaType当中的qualityValue的新的MediaType
     */
    open fun copyQualityValue(mediaType: MediaType): MediaType {
        if (!mediaType.parameters.containsKey(PARAM_QUALITY_FACTOR)) {
            return this
        }
        val params = LinkedHashMap(parameters)
        params[PARAM_QUALITY_FACTOR] = mediaType.parameters[PARAM_QUALITY_FACTOR]!!
        return MediaType(this, params)
    }

    /**
     * 将当前MediaType当中的qualityValue参数移除掉, 并得到新的[MediaType]实例对象
     *
     * @return 移除掉qualityValue参数之后得到的新的[MediaType]实例对象
     */
    open fun removeQualityValue(): MediaType {
        if (!parameters.containsKey(PARAM_QUALITY_FACTOR)) {
            return this
        }
        val params = LinkedHashMap(parameters)
        params.remove(PARAM_QUALITY_FACTOR)
        return MediaType(this, params)
    }

    @Suppress("UNUSED")
    companion object {

        /**
         * MediaType的权值参数名
         */
        private const val PARAM_QUALITY_FACTOR = "q"

        @JvmField
        val ALL = MediaType("*", "*")
        const val ALL_VALUE = "*/*"

        @JvmField
        val APPLICATION_ATOM_XML = MediaType("application", "atom+xml")
        const val APPLICATION_ATOM_XML_VALUE = "application/atom+xml"

        @JvmField
        val APPLICATION_CBOR = MediaType("application", "cbor")
        const val APPLICATION_CBOR_VALUE = "application/cbor"

        @JvmField
        val APPLICATION_FORM_URLENCODED = MediaType("application", "x-www-form-urlencoded")
        const val APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded"

        @JvmField
        val APPLICATION_JSON = MediaType("application", "json")
        const val APPLICATION_JSON_VALUE = "application/json"

        @JvmField
        val APPLICATION_JSON_UTF8 = MediaType("application", "json", StandardCharsets.UTF_8)
        const val APPLICATION_JSON_UTF8_VALUE = "application/json;charset=UTF-8"

        @JvmField
        val APPLICATION_OCTET_STREAM = MediaType("application", "octet-stream")
        const val APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream"

        @JvmField
        val APPLICATION_PDF = MediaType("application", "pdf")
        const val APPLICATION_PDF_VALUE = "application/pdf"

        @JvmField
        val APPLICATION_PROBLEM_JSON = MediaType("application", "problem+json")
        const val APPLICATION_PROBLEM_JSON_VALUE = "application/problem+json"

        @JvmField
        val APPLICATION_PROBLEM_JSON_UTF8 = MediaType("application", "problem+json", StandardCharsets.UTF_8)
        const val APPLICATION_PROBLEM_JSON_UTF8_VALUE = "application/problem+json;charset=UTF-8"

        @JvmField
        val APPLICATION_PROBLEM_XML = MediaType("application", "problem+xml")
        const val APPLICATION_PROBLEM_XML_VALUE = "application/problem+xml"

        @JvmField
        val APPLICATION_RSS_XML = MediaType("application", "rss+xml")
        const val APPLICATION_RSS_XML_VALUE = "application/rss+xml"

        @JvmField
        val APPLICATION_NDJSON = MediaType("application", "x-ndjson")
        const val APPLICATION_NDJSON_VALUE = "application/x-ndjson"

        @JvmField
        val APPLICATION_STREAM_JSON = MediaType("application", "stream+json")
        const val APPLICATION_STREAM_JSON_VALUE = "application/stream+json"

        @JvmField
        val APPLICATION_XHTML_XML = MediaType("application", "xhtml+xml")
        const val APPLICATION_XHTML_XML_VALUE = "application/xhtml+xml"

        @JvmField
        val APPLICATION_XML = MediaType("application", "xml")
        const val APPLICATION_XML_VALUE = "application/xml"

        @JvmField
        val IMAGE_GIF = MediaType("image", "gif")
        const val IMAGE_GIF_VALUE = "image/gif"

        @JvmField
        val IMAGE_JPEG = MediaType("image", "jpeg")
        const val IMAGE_JPEG_VALUE = "image/jpeg"

        @JvmField
        val IMAGE_PNG = MediaType("image", "png")
        const val IMAGE_PNG_VALUE = "image/png"

        @JvmField
        val MULTIPART_FORM_DATA = MediaType("multipart", "form-data")
        const val MULTIPART_FORM_DATA_VALUE = "multipart/form-data"

        @JvmField
        val MULTIPART_MIXED = MediaType("multipart", "mixed")
        const val MULTIPART_MIXED_VALUE = "multipart/mixed"

        @JvmField
        val MULTIPART_RELATED = MediaType("multipart", "related")
        const val MULTIPART_RELATED_VALUE = "multipart/related"

        @JvmField
        val TEXT_EVENT_STREAM = MediaType("text", "event-stream")
        const val TEXT_EVENT_STREAM_VALUE = "text/event-stream"

        @JvmField
        val TEXT_HTML = MediaType("text", "html")
        const val TEXT_HTML_VALUE = "text/html"

        @JvmField
        val TEXT_MARKDOWN = MediaType("text", "markdown")
        const val TEXT_MARKDOWN_VALUE = "text/markdown"

        @JvmField
        val TEXT_PLAIN = MediaType("text", "plain")
        const val TEXT_PLAIN_VALUE = "text/plain"

        @JvmField
        val TEXT_XML: MediaType = MediaType("text", "xml")
        const val TEXT_XML_VALUE = "text/xml"

        /**
         * 按照权值去对[MediaType]去进行排序的比较器
         */
        @JvmField
        val QUALITY_VALUE_COMPARATOR = Comparator { mediaType1: MediaType, mediaType2: MediaType ->
            val quality1 = mediaType1.qualityValue
            val quality2 = mediaType2.qualityValue
            val qualityComparison = quality2.compareTo(quality1)
            if (qualityComparison != 0) {
                return@Comparator qualityComparison // audio/*;q=0.7 < audio/*;q=0.3
            } else if (mediaType1.isWildcardType && !mediaType2.isWildcardType) {  // */* < audio/*
                return@Comparator 1
            } else if (mediaType2.isWildcardType && !mediaType1.isWildcardType) {  // audio/* > */*
                return@Comparator -1
            } else if (mediaType1.type != mediaType2.type) {  // audio/basic == text/html
                return@Comparator 0
            } else {  // mediaType1.getType().equals(mediaType2.getType())
                if (mediaType1.isWildcardSubtype && !mediaType2.isWildcardSubtype) {  // audio/* < audio/basic
                    return@Comparator 1
                } else if (mediaType2.isWildcardSubtype && !mediaType1.isWildcardSubtype) {  // audio/basic > audio/*
                    return@Comparator -1
                } else if (mediaType1.subtype != mediaType2.subtype) {  // audio/basic == audio/wave
                    return@Comparator 0
                } else {
                    val paramsSize1 = mediaType1.parameters.size
                    val paramsSize2 = mediaType2.parameters.size
                    return@Comparator paramsSize2.compareTo(paramsSize1) // audio/basic;level=1 < audio/basic
                }
            }
        }

        /**
         * 按照[MediaType]的具体程度去对[MediaType]去进行排序的比较器
         */
        @JvmStatic
        val SPECIFICITY_COMPARATOR: Comparator<MediaType> = object : SpecificityComparator<MediaType>() {
            override fun compareParameters(mediaType1: MediaType, mediaType2: MediaType): Int {
                val quality1 = mediaType1.qualityValue
                val quality2 = mediaType2.qualityValue
                val qualityComparison = quality2.compareTo(quality1)
                return if (qualityComparison != 0) {
                    qualityComparison // audio/*;q=0.7 < audio/*;q=0.3
                } else super.compareParameters(mediaType1, mediaType2)
            }
        }

        /**
         * 根据给定的MediaType字符串, 去解析成为[MediaType]
         *
         * @param value mediaType字符串
         * @return 解析得到的MediaType对象
         */
        @JvmStatic
        fun valueOf(value: String): MediaType {
            return parseMediaType(value)
        }

        @JvmStatic
        fun parseMediaType(mediaType: String): MediaType {
            val type = MimeTypeUtils.parseMimeType(mediaType)
            return MediaType(type)
        }

        @JvmStatic
        fun parseMediaTypes(@Nullable mediaTypes: String?): List<MediaType> {
            if (!StringUtils.hasLength(mediaTypes)) {
                return emptyList()
            }
            val tokenizedTypes = MimeTypeUtils.tokenize(mediaTypes)
            val result = ArrayList<MediaType>(tokenizedTypes.size)
            for (type in tokenizedTypes) {
                if (StringUtils.hasText(type)) {
                    result.add(parseMediaType(type))
                }
            }
            return result
        }

        @JvmStatic
        fun parseMediaTypes(mediaTypes: List<String>): List<MediaType> {
            return if (mediaTypes.isEmpty()) {
                emptyList()
            } else if (mediaTypes.size == 1) {
                parseMediaTypes(mediaTypes[0])
            } else {
                val result = ArrayList<MediaType>(8)
                for (mediaType in mediaTypes) {
                    result.addAll(parseMediaTypes(mediaType))
                }
                result
            }
        }

        /**
         * 将给定的[MimeType]列表当中的元素, 去进行逐一转换成为[MediaType]
         *
         * @param mimeTypes 原始的待转换MimeType列表
         * @return 转换之后得到的MediaType列表
         */
        @JvmStatic
        fun asMediaTypes(mimeTypes: List<MimeType>): List<MediaType> = mimeTypes.map(this::asMediaType)

        /**
         * 将给定的[MimeType]去转换成为[MediaType]
         *
         * @param mimeType 待转换的MimeType
         * @return 转换得到的MediaType
         */
        @JvmStatic
        fun asMediaType(mimeType: MimeType): MediaType {
            if (mimeType is MediaType) {
                return mimeType
            }
            return MediaType(mimeType.type, mimeType.subtype, mimeType.parameters)
        }

        @JvmStatic
        fun toString(mediaTypes: Collection<MediaType>): String {
            return MimeTypeUtils.toString(mediaTypes)
        }

        /**
         * 按照具体程度, 去对给定的[MediaType]列表去进行排序
         *
         * @param mediaTypes 待排序的MediaType列表
         */
        @JvmStatic
        fun sortBySpecificity(mediaTypes: MutableList<MediaType>) {
            if (mediaTypes.size > 1) {
                mediaTypes.sortWith(SPECIFICITY_COMPARATOR)
            }
        }

        /**
         * 按照权值去对给定的[MediaType]列表去进行排序
         *
         * @param mediaTypes 待排序的MediaType列表
         */
        @JvmStatic
        fun sortByQualityValue(mediaTypes: MutableList<MediaType>) {
            if (mediaTypes.size > 1) {
                mediaTypes.sortWith(QUALITY_VALUE_COMPARATOR)
            }
        }

        /**
         * 将给定的[MediaType]列表, 先按照具体程度去进行排序, 再按照权值去进行排序
         *
         * @param mediaTypes 待排序的MediaType列表
         */
        @JvmStatic
        fun sortBySpecificityAndQuality(mediaTypes: MutableList<MediaType>) {
            if (mediaTypes.size > 1) {
                mediaTypes.sortWith(SPECIFICITY_COMPARATOR.thenComparing(QUALITY_VALUE_COMPARATOR))
            }
        }
    }
}