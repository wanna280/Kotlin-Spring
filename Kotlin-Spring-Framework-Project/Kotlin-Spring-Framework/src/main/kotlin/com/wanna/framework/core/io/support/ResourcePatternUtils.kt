package com.wanna.framework.core.io.support

import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.lang.Nullable

/**
 * ResourcePattern的工具类，提供资源路径的表达式解析的相关工具方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 */
object ResourcePatternUtils {

    /**
     * 将ResourceLoader去转换成为ResourcePatternResolver
     *
     * @param resourceLoader ResourceLoader(可以为null，将会使用DefaultResultLoader)
     * @return 转换之后的ResourcePatternResolver
     */
    @JvmStatic
    fun getResourcePatternResolver(@Nullable resourceLoader: ResourceLoader?): ResourcePatternResolver {
        if (resourceLoader is ResourcePatternResolver) {
            return resourceLoader
        }
        return if (resourceLoader == null) PathMatchingResourcePatternResolver()
        else PathMatchingResourcePatternResolver(resourceLoader)
    }
}