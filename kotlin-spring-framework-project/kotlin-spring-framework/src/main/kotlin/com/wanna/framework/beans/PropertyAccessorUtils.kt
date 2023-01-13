package com.wanna.framework.beans

import com.wanna.framework.lang.Nullable

/**
 * 提供对于[PropertyAccessor]去对Bean的属性去提供访问的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/14
 */
object PropertyAccessorUtils {


    /**
     * 根据给定的propertyPath, 去转换得到属性名, 例如对于"person.address[0]", 将会得到"person.address"
     *
     * @param propertyPath 原始的propertyPath
     * @return 切割掉多余的indexed/mapped访问的部分的字符串
     */
    @JvmStatic
    fun getPropertyName(propertyPath: String): String {
        var separatorIndex = -1
        if (propertyPath.endsWith(PropertyAccessor.PROPERTY_KEY_SUFFIX)) {
            separatorIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX)
        }
        return if (separatorIndex == -1) propertyPath else propertyPath.substring(separatorIndex)
    }

    /**
     * 检查给定的propertyPath, 是否是一个嵌套的属性路径?
     *
     * @param propertyPath propertyPath
     * @return 只要路径当中含有"."/"[", 那么将会return true; 否则return false
     */
    @JvmStatic
    fun isNestedOrIndexedProperty(@Nullable propertyPath: String?): Boolean {
        propertyPath ?: return false
        propertyPath.forEach {
            if (it == PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR || it == PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) {
                return true
            }
        }
        return false
    }

    /**
     * 计算给定的propertyPath当中的第一个"."的位置index(不考虑`map[my.keys]`这种Key当中存在的".")
     *
     * @param propertyPath propertyPath
     * @return 给定的propertyPath当中第一个"."的位置index(不存在"."的话, 那么return -1)
     */
    @JvmStatic
    fun getFirstNestedPropertySeparatorIndex(propertyPath: String): Int {
        return getNestedPropertySeparatorIndex(propertyPath, false)
    }

    /**
     * 计算给定的propertyPath当中的最后"."的位置index(不考虑`map[my.keys]`这种Key当中存在的".")
     *
     * @param propertyPath propertyPath
     * @return 给定的propertyPath当中最后一个"."的位置index(不存在"."的话, 那么return -1)
     */
    @JvmStatic
    fun getLastNestedPropertySeparatorIndex(propertyPath: String): Int {
        return getNestedPropertySeparatorIndex(propertyPath, true)
    }

    /**
     * 计算propertyPath的第一个/最后一个"."的位置(不考虑`map[my.keys]`这种Key当中存在的".")
     *
     * @param propertyPath propertyPath
     * @param last 为true计算最后一个, 为false就算第一个
     * @return 给定的propertyPath当中第一个/最后一个"."的位置index(不存在"."的话, 那么return -1)
     */
    @JvmStatic
    private fun getNestedPropertySeparatorIndex(propertyPath: String, last: Boolean): Int {
        var inIndex = false
        val length = propertyPath.length
        var index = if (last) propertyPath.length - 1 else 0
        while (if (last) index >= 0 else index < length) {
            // 如果当前是"["/"]", 将当前是否在index当中的标志位取反...
            if (propertyPath[index] == PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR
                || propertyPath[index] == PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR
            ) {
                inIndex = !inIndex

                // 如果遇到了".", 那么需要检查一下, 当前是否在"[]"内部?
            } else if (propertyPath[index] == PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR) {
                // 如果不在Key的内部, 那么直接return; 如果在Key内部, 那么继续搜索
                if (!inIndex) {
                    return index
                }
            }
            // 根据last, 去决定index前进的方向
            if (last) {
                index--
            } else {
                index++
            }
        }
        return -1
    }


}