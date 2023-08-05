package com.wanna.framework.core.io

import com.wanna.framework.lang.Nullable

/**
 * 协议的解析器, 是一个策略接口, 供完成资源的加载
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 */
fun interface ProtocolResolver {

    /**
     * 将给定的location去解析成为Resource
     *
     * @param location 资源路径
     * @param resourceLoader 资源加载器
     * @return 根据location去解析到的资源(解析不到的话为null)
     */
    @Nullable
    fun resolve(location: String, resourceLoader: ResourceLoader): Resource?
}