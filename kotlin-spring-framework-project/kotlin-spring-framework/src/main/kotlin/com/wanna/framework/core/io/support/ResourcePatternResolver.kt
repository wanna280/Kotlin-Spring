package com.wanna.framework.core.io.support

import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import java.io.IOException

/**
 * 支持去进行表达式解析的ResourceLoader
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 */
interface ResourcePatternResolver : ResourceLoader {
    companion object {
        const val CLASSPATH_ALL_URL_PREFIX = "classpath*:"
    }

    /**
     * 根据给定的位置表达式，去解析出来合适的资源
     *
     * @param locationPattern 资源位置的表达式
     * @return 解析得到的资源列表
     */
    @kotlin.jvm.Throws(IOException::class)
    fun getResources(locationPattern: String): Array<Resource>
}