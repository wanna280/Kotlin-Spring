package com.wanna.framework.core.io

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ResourceUtils

/**
 * Spring家的资源加载器的顶层接口
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 */
interface ResourceLoader {
    companion object {
        /**
         * ClassPath的前缀
         */
        const val CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX
    }

    /**
     * 根据给定的资源的位置，去加载Resource
     *
     * @param location location
     * @return Resource
     * @see CLASSPATH_URL_PREFIX
     * @see Resource
     */
    fun getResource(location: String): Resource

    /**
     * 获取到该资源加载器进行资源加载时所使用到的ClassLoader
     *
     * @return ClassLoader(or null)
     */
    @Nullable
    fun getClassLoader(): ClassLoader?
}