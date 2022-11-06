package com.wanna.framework.test.context.support

import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.test.context.TestPropertySource

/**
 * TestPropertySource的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
object TestPropertySourceUtils {

    /**
     * 解析testClass上的[TestPropertySource]注解
     *
     * @param testClass testClass
     * @return MergedTestPropertySources
     */
    @JvmStatic
    fun buildMergedTestPropertySources(testClass: Class<*>): MergedTestPropertySources {
        val testPropertySource = AnnotatedElementUtils.getMergedAnnotation(testClass, TestPropertySource::class.java)
            ?: return MergedTestPropertySources()
        return MergedTestPropertySources()
    }

}