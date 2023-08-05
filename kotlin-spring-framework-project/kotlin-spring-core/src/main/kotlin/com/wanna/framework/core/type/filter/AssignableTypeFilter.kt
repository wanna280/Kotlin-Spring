package com.wanna.framework.core.type.filter

/**
 * 匹配类型, 判断给定的类是否是parentClass的子类?
 *
 * @param targetType parentClass
 */
open class AssignableTypeFilter(private val targetType: Class<*>) : AbstractTypeHierarchyTraversingFilter() {

    override fun matchClassName(className: String): Boolean {
        return className == targetType.name
    }

    override fun matchSuperClass(superClassName: String): Boolean? {
        return superClassName == targetType.name
    }

    override fun matchInterface(interfaceName: String): Boolean? {
        return interfaceName == targetType.name
    }
}