package com.wanna.framework.context.annotation

import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是一个Import配置类的注册中心
 */
interface ImportRegistry {
    /**
     * 为被导入的配置类，获取到导入它的配置类的相关注解信息
     *
     * @param importedClass 被导入的配置类的类名
     * @return 导入它的配置类的注解信息(如果获取不到return null)
     */
    fun getImportingClassFor(importedClass: String): AnnotationMetadata?

    /**
     * 将Import的配置类(importingClass)的全部信息remove掉
     *
     * @param importingClass Import配置类，而不是被Import的配置类
     */
    fun removeImportingClass(importingClass: String)
}