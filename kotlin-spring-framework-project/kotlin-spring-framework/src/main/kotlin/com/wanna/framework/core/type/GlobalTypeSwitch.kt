package com.wanna.framework.core.type

/**
 * 临时测试的开关
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/1
 */
object GlobalTypeSwitch {

    /**
     * ComponentScan的开关是否打开?
     */
    fun isComponentScanOpen(): Boolean = true

    /**
     * AnnotationMetadata的开关是否打开?
     */
    fun isAnnotationMetadataOpen(): Boolean = true
}