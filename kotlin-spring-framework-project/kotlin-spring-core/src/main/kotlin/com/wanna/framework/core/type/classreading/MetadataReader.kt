package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.ClassMetadata

/**
 * 对外提供Class的访问的Reader, 基于ASM的方式去提供访问
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 */
interface MetadataReader {

    /**
     * 获取到该Class对应的Resource资源
     */
    val resource: Resource

    /**
     * 获取到该Class对应的元信息(比如包名/类名等)
     */
    val classMetadata: ClassMetadata

    /**
     * 获取该Class对应的注解元信息
     */
    val annotationMetadata: AnnotationMetadata
}