package com.wanna.framework.test.context.support

/**
 * TestPropertySource的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
object TestPropertySourceUtils {

    @JvmStatic
    fun buildMergedTestPropertySources(testClass: Class<*>): MergedTestPropertySources {
        return MergedTestPropertySources()
    }

}