package com.wanna.framework.core.type

/**
 * 临时测试的开关
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/1
 */
object GlobalTypeSwitch {
    @JvmField
    var componentScanOpen = true

    @JvmField
    var annotatedElementUtilsOpen = true

    @JvmField
    var annotationMetadataOpen = true

    /**
     * ComponentScan的开关是否打开?
     */
    @JvmStatic
    fun isComponentScanOpen(): Boolean = componentScanOpen

    /**
     * AnnotationMetadata的开关是否打开?
     */
    @JvmStatic
    fun isAnnotationMetadataOpen(): Boolean = annotationMetadataOpen

    /**
     * AnnotatedElementUtils的开关是否打开?
     */
    @JvmStatic
    fun isAnnotatedElementUtilsOpen(): Boolean = annotatedElementUtilsOpen
}