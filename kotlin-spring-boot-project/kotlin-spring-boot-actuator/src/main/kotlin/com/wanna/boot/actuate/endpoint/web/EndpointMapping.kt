package com.wanna.boot.actuate.endpoint.web

import com.wanna.framework.util.StringUtils

/**
 * Endpoint的Mapping, 主要负责完成路径的配置工作
 *
 * @param basePath Endpoint端点的basePath(路径前缀, 所有的endpoint都需要加上这个前缀去进行访问)
 */
open class EndpointMapping(basePath: String) {
    private val basePath = normalizePath(basePath)

    /**
     * 获取Endpoint的基础路径(路径前缀)
     *
     * @return Endpoint的basePath(类似contextPath)
     */
    open fun getPath(): String = this.basePath

    /**
     * 针对于basePath, 去创建subPath, 比如basePath="/actuator",
     * path="/env", 那么最终就得到了"/actuator/env"这样的最终路径
     *
     * @param path subPath
     * @return 将subPath拼接到basePath之后的结果
     */
    open fun createSubPath(path: String): String = basePath + normalizePath(path)

    /**
     * * 如果路径开头没带"/", 那么补上"/";
     * * 如果路径结束带了"/", 那么把末尾的"/去掉";
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