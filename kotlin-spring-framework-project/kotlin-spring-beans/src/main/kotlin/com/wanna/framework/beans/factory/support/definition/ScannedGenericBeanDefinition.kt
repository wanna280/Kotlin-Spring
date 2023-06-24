package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.MethodMetadata
import com.wanna.framework.core.type.classreading.MetadataReader

/**
 * 这是在扫描的过程中创建的beanDefinition, 扫描过程中拿到的BeanDefinition,
 * 本身一定是因为标注了注解被扫描进来的, 因此它一定是AnnotatedBeanDefinition
 *
 * @see GenericBeanDefinition
 * @see AnnotatedBeanDefinition
 */
open class ScannedGenericBeanDefinition protected constructor() : AnnotatedBeanDefinition, GenericBeanDefinition() {
    /**
     * BeanDefinition的类的Metadata注解元信息
     */
    private var metadata: AnnotationMetadata? = null

    /**
     * 基于[MetadataReader]的方式去构建[ScannedGenericBeanDefinition]
     *
     * @param metadataReader MetadataReader
     */
    constructor(metadataReader: MetadataReader) : this() {
        // 设置AnnotationMetadata
        this.metadata = metadataReader.annotationMetadata

        // setResource
        this.setResource(metadataReader.resource)

        // set BeanClassName
        this.setBeanClassName(metadataReader.annotationMetadata.getClassName())
    }


    override fun getMetadata() = metadata ?: throw IllegalStateException("AnnotationMetadata is null")

    override fun getFactoryMethodMetadata(): MethodMetadata? = null
}