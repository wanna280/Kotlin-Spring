package com.wanna.framework.web.http

import org.springframework.util.*
import java.io.Serializable
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * 这个类从Spring源码当中直接去进行移植的, 目的是描述HTTP请求当中的MediaType(媒体类型)
 */
class MediaType : MimeType, Serializable {
    constructor(type: String) : super(type)
    constructor(type: String, subtype: String) : super(type, subtype, emptyMap<String, String>())
    constructor(type: String, subtype: String, charset: Charset) : super(type, subtype, charset)
    constructor(type: String, subtype: String, qualityValue: Double) : this(
        type, subtype, mapOf(PARAM_QUALITY_FACTOR to qualityValue.toString())
    )

    constructor(other: MediaType, charset: Charset) : super(other, charset)
    constructor(other: MediaType, parameters: Map<String, String>) : super(other.type, other.subtype, parameters)
    constructor(type: String, subtype: String, parameters: Map<String, String>) : super(type, subtype, parameters)
    constructor(mimeType: MimeType) : super(mimeType) {
        parameters.forEach { (parameter, value) -> checkParameters(parameter, value) }
    }

    override fun checkParameters(parameter: String, value: String) {
        var value0 = value
        super.checkParameters(parameter, value0)
        if (PARAM_QUALITY_FACTOR == parameter) {
            value0 = unquote(value0)
            val d = value.toDouble()
            Assert.isTrue(d in 0.0..1.0, "Invalid quality value \"$value\": should be between 0.0 and 1.0")
        }
    }

    val qualityValue: Double
        get() {
            val qualityFactor = getParameter(PARAM_QUALITY_FACTOR)
            return if (qualityFactor != null) unquote(qualityFactor).toDouble() else 1.0
        }

    fun includes(other: MediaType?): Boolean {
        return super.includes(other)
    }

    fun isCompatibleWith(other: MediaType?): Boolean {
        return super.isCompatibleWith(other)
    }

    fun copyQualityValue(mediaType: MediaType): MediaType {
        if (!mediaType.parameters.containsKey(PARAM_QUALITY_FACTOR)) {
            return this
        }
        val params: MutableMap<String, String> = LinkedHashMap(
            parameters
        )
        params[PARAM_QUALITY_FACTOR] = mediaType.parameters[PARAM_QUALITY_FACTOR]!!
        return MediaType(this, params)
    }

    fun removeQualityValue(): MediaType {
        if (!parameters.containsKey(PARAM_QUALITY_FACTOR)) {
            return this
        }
        val params: MutableMap<String, String> = LinkedHashMap(
            parameters
        )
        params.remove(PARAM_QUALITY_FACTOR)
        return MediaType(this, params)
    }

    @Suppress("UNUSED")
    companion object {
        val ALL: MediaType = MediaType("*", "*")
        const val ALL_VALUE = "*/*"
        val APPLICATION_ATOM_XML = MediaType("application", "atom+xml")
        const val APPLICATION_ATOM_XML_VALUE = "application/atom+xml"
        val APPLICATION_CBOR = MediaType("application", "cbor")
        const val APPLICATION_CBOR_VALUE = "application/cbor"
        val APPLICATION_FORM_URLENCODED = MediaType("application", "x-www-form-urlencoded")
        const val APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded"
        val APPLICATION_JSON = MediaType("application", "json")
        const val APPLICATION_JSON_VALUE = "application/json"
        val APPLICATION_JSON_UTF8 = MediaType("application", "json", StandardCharsets.UTF_8)
        const val APPLICATION_JSON_UTF8_VALUE = "application/json;charset=UTF-8"
        val APPLICATION_OCTET_STREAM = MediaType("application", "octet-stream")
        const val APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream"
        val APPLICATION_PDF = MediaType("application", "pdf")
        const val APPLICATION_PDF_VALUE = "application/pdf"
        val APPLICATION_PROBLEM_JSON = MediaType("application", "problem+json")
        const val APPLICATION_PROBLEM_JSON_VALUE = "application/problem+json"
        val APPLICATION_PROBLEM_JSON_UTF8 = MediaType("application", "problem+json", StandardCharsets.UTF_8)
        const val APPLICATION_PROBLEM_JSON_UTF8_VALUE = "application/problem+json;charset=UTF-8"
        val APPLICATION_PROBLEM_XML = MediaType("application", "problem+xml")
        const val APPLICATION_PROBLEM_XML_VALUE = "application/problem+xml"
        val APPLICATION_RSS_XML = MediaType("application", "rss+xml")
        const val APPLICATION_RSS_XML_VALUE = "application/rss+xml"
        val APPLICATION_NDJSON = MediaType("application", "x-ndjson")
        const val APPLICATION_NDJSON_VALUE = "application/x-ndjson"
        val APPLICATION_STREAM_JSON = MediaType("application", "stream+json")
        const val APPLICATION_STREAM_JSON_VALUE = "application/stream+json"
        val APPLICATION_XHTML_XML = MediaType("application", "xhtml+xml")
        const val APPLICATION_XHTML_XML_VALUE = "application/xhtml+xml"
        val APPLICATION_XML = MediaType("application", "xml")
        const val APPLICATION_XML_VALUE = "application/xml"
        val IMAGE_GIF = MediaType("image", "gif")
        const val IMAGE_GIF_VALUE = "image/gif"
        val IMAGE_JPEG = MediaType("image", "jpeg")
        const val IMAGE_JPEG_VALUE = "image/jpeg"
        val IMAGE_PNG = MediaType("image", "png")
        const val IMAGE_PNG_VALUE = "image/png"
        val MULTIPART_FORM_DATA = MediaType("multipart", "form-data")
        const val MULTIPART_FORM_DATA_VALUE = "multipart/form-data"
        val MULTIPART_MIXED = MediaType("multipart", "mixed")
        const val MULTIPART_MIXED_VALUE = "multipart/mixed"
        val MULTIPART_RELATED = MediaType("multipart", "related")
        const val MULTIPART_RELATED_VALUE = "multipart/related"
        val TEXT_EVENT_STREAM = MediaType("text", "event-stream")
        const val TEXT_EVENT_STREAM_VALUE = "text/event-stream"
        val TEXT_HTML = MediaType("text", "html")
        const val TEXT_HTML_VALUE = "text/html"
        val TEXT_MARKDOWN = MediaType("text", "markdown")
        const val TEXT_MARKDOWN_VALUE = "text/markdown"
        val TEXT_PLAIN = MediaType("text", "plain")
        const val TEXT_PLAIN_VALUE = "text/plain"
        val TEXT_XML: MediaType = MediaType("text", "xml")
        const val TEXT_XML_VALUE = "text/xml"
        private const val PARAM_QUALITY_FACTOR = "q"

        fun valueOf(value: String): MediaType {
            return parseMediaType(value)
        }

        fun parseMediaType(mediaType: String): MediaType {
            val type: MimeType = try {
                MimeTypeUtils.parseMimeType(mediaType)
            } catch (ex: InvalidMimeTypeException) {
                throw RuntimeException()
            }
            return try {
                MediaType(type)
            } catch (ex: IllegalArgumentException) {
                throw IllegalArgumentException()
            }
        }

        fun parseMediaTypes(mediaTypes: String?): List<MediaType> {
            if (!StringUtils.hasLength(mediaTypes)) {
                return emptyList()
            }
            // Avoid using java.util.stream.Stream in hot paths
            val tokenizedTypes = MimeTypeUtils.tokenize(mediaTypes!!)
            val result: MutableList<MediaType> = ArrayList(tokenizedTypes.size)
            for (type in tokenizedTypes) {
                if (StringUtils.hasText(type)) {
                    result.add(parseMediaType(type))
                }
            }
            return result
        }

        fun parseMediaTypes(mediaTypes: List<String>): List<MediaType> {
            return if (CollectionUtils.isEmpty(mediaTypes)) {
                emptyList()
            } else if (mediaTypes.size == 1) {
                parseMediaTypes(mediaTypes[0])
            } else {
                val result: MutableList<MediaType> = ArrayList(8)
                for (mediaType in mediaTypes) {
                    result.addAll(parseMediaTypes(mediaType))
                }
                result
            }
        }

        fun asMediaTypes(mimeTypes: List<MimeType>): List<MediaType> {
            val mediaTypes: MutableList<MediaType> = ArrayList(mimeTypes.size)
            for (mimeType in mimeTypes) {
                mediaTypes.add(asMediaType(mimeType))
            }
            return mediaTypes
        }

        fun asMediaType(mimeType: MimeType): MediaType {
            return if (mimeType is MediaType) {
                mimeType
            } else MediaType(mimeType.type, mimeType.subtype, mimeType.parameters)
        }

        fun toString(mediaTypes: Collection<MediaType>): String {
            return MimeTypeUtils.toString(mediaTypes)
        }

        fun sortBySpecificity(mediaTypes: MutableList<MediaType>) {
            if (mediaTypes.size > 1) {
                mediaTypes.sortWith(SPECIFICITY_COMPARATOR)
            }
        }

        fun sortByQualityValue(mediaTypes: List<MediaType?>) {
            Assert.notNull(mediaTypes, "'mediaTypes' must not be null")
            if (mediaTypes.size > 1) {
                mediaTypes.sortedWith(QUALITY_VALUE_COMPARATOR)
            }
        }

        fun sortBySpecificityAndQuality(mediaTypes: List<MediaType?>) {
            Assert.notNull(mediaTypes, "'mediaTypes' must not be null")
            if (mediaTypes.size > 1) {
                mediaTypes.sortedWith(SPECIFICITY_COMPARATOR.thenComparing(QUALITY_VALUE_COMPARATOR))
            }
        }

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
    }
}