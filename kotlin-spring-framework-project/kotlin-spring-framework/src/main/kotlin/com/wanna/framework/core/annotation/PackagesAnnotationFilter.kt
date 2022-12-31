package com.wanna.framework.core.annotation

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 */
class PackagesAnnotationFilter(vararg packages: String) : AnnotationFilter {

    private val prefixes: Array<String> = packages.map { "$it." }.toTypedArray()

    override fun matches(typeName: String): Boolean {
        prefixes.forEach {
            if (typeName.startsWith(it)) {
                return true
            }
        }
        return false
    }
}