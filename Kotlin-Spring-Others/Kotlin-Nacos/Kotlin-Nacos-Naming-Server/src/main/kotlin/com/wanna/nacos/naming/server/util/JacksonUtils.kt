package com.wanna.nacos.naming.server.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * Jackson的工具类
 */
object JacksonUtils {
    private val objectMapper = ObjectMapper()

    /**
     * 将一个Java对象转为Json数据
     *
     * @param obj 要去进行转换的Java对象
     * @return 转换成为的Json字符串
     */
    @JvmStatic
    fun toJson(obj: Any): String {
        return objectMapper.writeValueAsString(obj)
    }

    /**
     * 创建一个空的ArrayNode
     *
     * @return 空的Jackson的ArrayNode
     */
    @JvmStatic
    fun createEmptyArrayNode(): ArrayNode {
        return ArrayNode(objectMapper.nodeFactory)
    }

    /**
     * 创建一个空的ObjectNode
     *
     * @return 空的ObjectNode
     */
    @JvmStatic
    fun createEmptyObjectNode(): ObjectNode {
        return ObjectNode(objectMapper.nodeFactory)
    }
}