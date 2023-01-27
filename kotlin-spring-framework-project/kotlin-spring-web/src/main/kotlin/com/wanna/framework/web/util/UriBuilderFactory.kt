package com.wanna.framework.web.util

/**
 * [UriBuilder]的工厂, 提供[UriBuilder]的相关工厂方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @see UriTemplateHandler
 * @see UriBuilder
 * @see DefaultUriBuilderFactory
 */
interface UriBuilderFactory : UriTemplateHandler {

    /**
     * 根据初始的[uriTemplate]去构建[UriBuilder]
     *
     * @param uriTemplate 初始的uriTemplate
     * @return URI Builder
     */
    fun uriString(uriTemplate: String): UriBuilder

    /**
     * 构建一个空的[UriBuilder]
     *
     * @return URI Builder
     */
    fun builder(): UriBuilder
}