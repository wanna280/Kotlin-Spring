package com.wanna.boot.actuate.endpoint.web

import org.springframework.util.StringUtils

/**
 * Endpoint的Mapping，主要负责完成路径的配置工作
 *
 * @param basePath Endpoint断点的basePath
 */
open class EndpointMapping(basePath: String) {
    private val basePath = normalizePath(basePath)

    /**
     * 获取包装的基础路径
     *
     * @return Endpoint的basePath(类似contextPath)
     */
    open fun getPath(): String = this.basePath

    /**
     * 针对于basePath，去创建subPath
     *
     * @param path subPath
     * @return 将subPath拼接到basePath之后的结果
     */
    open fun createSubPath(path: String): String {
        return basePath + normalizePath(path)
    }

    /**
     * * 如果路径开头没带"/"，那么补上"/"；
     * * 如果路径结束带了"/"，那么把末尾的"/去掉"；
     *
     * @param path 原始的要去进行转换的路径
     * @return 转换之后的路径
     */
    private fun normalizePath(path: String): String {
        if (!StringUtils.hasText(path)) {
            return path
        }
        var normalizedPath = path
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/$normalizedPath"
        }
        if (normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length - 1)
        }
        return normalizedPath
    }
}