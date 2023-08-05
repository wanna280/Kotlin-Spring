package com.wanna.framework.web.http

import com.wanna.framework.core.io.Resource
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.LinkedMultiValueMap
import com.wanna.framework.util.MultiValueMap
import com.wanna.framework.util.StringUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * 提供处理文件名(filename)/[Resource]与[MediaType]之间的映射关系的工厂方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 *
 * @see MediaType
 */
object MediaTypeFactory {
    /**
     * MimeTypes的文件名(使用绝对路径),
     *
     * 文件当中的每一行的格式为"mediaType extension1 extension2 extension3 ...", 每一行的第一个元素为mediaType, 后面的都为文件扩展名
     */
    private const val MIME_TYPES_FILE_NAME = "/com/wanna/framework/web/http/mime.types"

    /**
     * Key-文件扩展名, Value-该文件扩展名对应的MediaTypes
     */
    @JvmStatic
    private val fileExtensionToMediaTypes: MultiValueMap<String, MediaType> = parseMimeTypes()

    /**
     * 从配置文件[MIME_TYPES_FILE_NAME]当中去进行解析MimeTypes, 对于遇到了使用"#"开头的注释, 以及空白行, 可以去自动跳过
     *
     * @return 解析得到的MimeTypes(Key-文件扩展名, Value-该文件扩展名对应的[MediaType])
     */
    @JvmStatic
    private fun parseMimeTypes(): MultiValueMap<String, MediaType> {
        val inputStream = MediaTypeFactory::class.java.getResourceAsStream(MIME_TYPES_FILE_NAME)
            ?: throw IllegalStateException("$MIME_TYPES_FILE_NAME not found in classpath")
        try {
            BufferedReader(InputStreamReader(inputStream, StandardCharsets.US_ASCII))
                .use {
                    val mimeTypes = LinkedMultiValueMap<String, MediaType>()
                    var line = it.readLine()
                    while (line != null) {

                        // 如果是空白行/注释, 那么pass掉
                        if (line.isEmpty() || line[0] == '#') {
                            line = it.readLine()
                            continue
                        }

                        // 不是空白行的话, 那么进行字符串分割(格式为"mediaType extension1 extension2 extension3")
                        val tokens = StringUtils.tokenizeToStringArray(line, " \t\n\r")
                        val mediaType = MediaType.parseMediaType(tokens[0])
                        for (i in 1 until tokens.size) {
                            val fileExtension = tokens[i].lowercase(Locale.ENGLISH)
                            mimeTypes.add(fileExtension, mediaType)
                        }
                        line = it.readLine()
                    }
                    return mimeTypes
                }
        } catch (ex: IOException) {
            throw IllegalStateException("Could not read $MIME_TYPES_FILE_NAME", ex)
        }
    }

    /**
     * 根据给定的[Resource]文件名, 从中提取到文件扩展名, 去获取对应的[MediaType],
     *
     * Note: 如果遇到了该后缀名存在有多个[MediaType], 返回第一个
     *
     * @param resource resource
     * @return 该文件名对应的文件扩展名对应的[MediaType]
     */
    @JvmStatic
    fun getMediaType(@Nullable resource: Resource?): Optional<MediaType> {
        return Optional.ofNullable(resource).map(Resource::getFilename).flatMap(this::getMediaType)
    }

    /**
     * 根据文件名, 从中提取到文件扩展名, 去获取对应的[MediaType],
     *
     * Note: 如果遇到了该后缀名存在有多个[MediaType], 返回第一个
     *
     * @param filename 文件名
     * @return 该文件名对应的文件扩展名对应的[MediaType]
     */
    @JvmStatic
    fun getMediaType(@Nullable filename: String?): Optional<MediaType> {
        return Optional.ofNullable(getMediaTypes(filename).firstOrNull())
    }

    /**
     * 根据文件名, 从中提取到文件扩展名, 去获取对应的[MediaType]
     *
     * @param filename 文件名
     * @return 该文件名对应的文件扩展名对应的[MediaType]列表
     */
    @JvmStatic
    fun getMediaTypes(@Nullable filename: String?): List<MediaType> {
        val extension = StringUtils.getFilenameExtension(filename) ?: return emptyList()
        return fileExtensionToMediaTypes[extension] ?: emptyList()
    }
}