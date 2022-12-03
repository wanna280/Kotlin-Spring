package com.wanna.nacos.config.server.enums

/**
 * NacosConfig的配置文件类型枚举
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 *
 * @param fileType 文件后缀名
 * @param contentType 响应类型(对于该种类型的配置文件应该怎么去进行响应?)
 */
enum class FileTypeEnum(val fileType: String, val contentType: String) {
    YML("yml", "text/plain;charset=UTF-8"),
    YAML("yml", "text/plain;charset=UTF-8"),
    TXT("text", "text/plain;charset=UTF-8"),
    TEXT("text", "text/plain;charset=UTF-8"),
    JSON("json", "application/json;charset=UTF-8"),
    XML("xml", "application/xml;charset=UTF-8"),
    PROPERTIES("properties", "text/plain;charset=UTF-8");

    companion object {
        /**
         * 根据"fileType"去获取到对应的[FileTypeEnum]枚举值
         *
         * @param fileType fileType
         * @return 对应的[FileTypeEnum]枚举值, 如果没有找到任何一个匹配的, 那么return "TEXT"
         */
        @JvmStatic
        fun getFileTypeEnumByFileExtensionOrFileType(fileType: String): FileTypeEnum {
            if (fileType.isNotEmpty()) {
                values().forEach {
                    if (it.name == fileType.uppercase()) {
                        return it
                    }
                }
            }
            return TEXT
        }
    }
}