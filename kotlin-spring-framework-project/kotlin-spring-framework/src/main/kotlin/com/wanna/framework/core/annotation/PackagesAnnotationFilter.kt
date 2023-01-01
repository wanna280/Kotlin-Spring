package com.wanna.framework.core.annotation

/**
 * 基于package去进行过滤的AnnotationFilter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 *
 * @param packages 要去进行匹配的包
 */
class PackagesAnnotationFilter(vararg packages: String) : AnnotationFilter {

    /**
     * 对packageName添加后缀"."
     */
    private val prefixes: Array<String> = packages.map { "$it." }.toTypedArray()

    /**
     * 针对给定的annotationClassName, 利用所有的Package去进行匹配, 匹配上其中一个就return true
     *
     * @param typeName annotationClassName
     * @return 如果匹配上了其中一个package, 那么return true; 否则return false
     */
    override fun matches(typeName: String): Boolean {
        prefixes.forEach {
            if (typeName.startsWith(it)) {
                return true
            }
        }
        return false
    }
}