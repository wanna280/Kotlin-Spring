package com.wanna.framework.core.io.support

import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import java.io.IOException
import kotlin.jvm.Throws

/**
 * 支持去进行表达式解析的ResourceLoader
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 *
 * @see ResourceLoader
 * @see Resource
 */
interface ResourcePatternResolver : ResourceLoader {
    companion object {

        /**
         * 对于"classpath:"来说, 将会根据给定的资源路径, 去加载到第一个Resource;
         * 对于"classpath*:"来说, 会根据资源路径去加载到该路径下的全部符合要求的Resource.
         * 例如, 很可能存在有多个Jar包当中都存在有"beans.xml"这个配置文件的情况, 这时候对于全部的
         * "beans.xml"的Resource的加载就很有必要
         *
         * @see ResourceLoader.CLASSPATH_URL_PREFIX
         */
        const val CLASSPATH_ALL_URL_PREFIX = "classpath*:"
    }

    /**
     * 根据给定的位置表达式，去解析出来合适的资源
     *
     * @param locationPattern 资源位置的表达式
     * @return 解析得到的资源列表
     */
    @Throws(IOException::class)
    fun getResources(locationPattern: String): Array<Resource>
}