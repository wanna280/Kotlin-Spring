package com.wanna.nacos.config.server.utils

/**
 * Namespace的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/14
 */
object NamespaceUtils {

    /**
     * public namespace常量
     */
    private const val NAMESPACE_PUBLIC_KEY = "public"

    /**
     * null namespace常量
     */
    private const val NAMESPACE_NULL_KEY = "null"


    /**
     * 根据传递的namespace去决定要采用的最终namespace;
     * 对于给定的是"public"/"null"/null这些情况, 都应该使用""去作为namespace;
     *
     * @param namespace namespace
     * @return 经过转换之后的namespace
     */
    @JvmStatic
    fun processNamespaceParameter(namespace: String?): String {
        if (namespace == null || namespace.isBlank()
            || NAMESPACE_PUBLIC_KEY.equals(namespace)
            || NAMESPACE_NULL_KEY.equals(namespace, false)
        ) {
            return ""
        }
        return namespace.trim()
    }
}