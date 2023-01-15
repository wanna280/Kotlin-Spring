package com.wanna.framework.test.context.support

import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.test.context.ContextConfiguration
import com.wanna.framework.test.context.ContextConfigurationAttributes

/**
 * ContextLoader的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
object ContextLoaderUtils {

    /**
     * 解析testClass上的[ContextConfiguration]注解的相关属性成为[ContextConfigurationAttributes]
     *
     * @param testClass testClass
     * @return 解析得到的[ContextConfigurationAttributes]列表
     */
    @JvmStatic
    fun resolveContextConfigurationAttributes(testClass: Class<*>): List<ContextConfigurationAttributes> {
        val attributesList = ArrayList<ContextConfigurationAttributes>()
        var annotation = AnnotatedElementUtils.getMergedAnnotation(testClass, ContextConfiguration::class.java)!!

        // 寻找当前testClass以及testClass的所有父类, 去寻找@ContextConfiguration注解
        var clazz: Class<*>? = testClass
        while (clazz != null && clazz != Any::class.java) {
            convertContextConfigToConfigAttributesAndAddToList(testClass, attributesList, annotation)
            annotation = AnnotatedElementUtils.getMergedAnnotation(clazz, ContextConfiguration::class.java)!!
            clazz = clazz.superclass
        }

        return attributesList
    }

    @JvmStatic
    private fun convertContextConfigToConfigAttributesAndAddToList(
        declaringClass: Class<*>,
        attributesList: MutableList<ContextConfigurationAttributes>,
        contextConfiguration: ContextConfiguration
    ) {
        attributesList += ContextConfigurationAttributes(declaringClass, contextConfiguration)
    }
}