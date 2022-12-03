package com.wanna.boot.context.properties.bind

import com.wanna.framework.lang.Nullable

/**
 * 占位符的解析器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
fun interface PlaceholdersResolver {

    companion object {

        /**
         * 不进行占位符解析的[PlaceholdersResolver]常量
         */
        @JvmStatic
        val NONE = PlaceholdersResolver { it }
    }


    /**
     * 占位符解析
     *
     * @param value 原始值
     * @return 经过占位符解析之后的值
     */
    @Nullable
    fun resolvePlaceholder(@Nullable value: Any?): Any?
}