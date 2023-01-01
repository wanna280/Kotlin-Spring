package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.classreading.MetadataReader
import com.wanna.framework.core.type.classreading.SimpleMetadataReader

/**
 * 这是在扫描的过程中创建的beanDefinition，扫描过程中拿到的BeanDefinition，
 * 本身一定是因为标注了注解被扫描进来的，因此它一定是AnnotatedBeanDefinition
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
     * 旧逻辑, 基于beanClass的方式去进行构建
     *
     * @param beanClass beanClass
     */
    constructor(beanClass: Class<*>) : this() {
        this.metadata = AnnotationMetadata.introspect(beanClass)
        this.setBeanClass(beanClass)
        this.setBeanClassName(beanClass.name)
    }

    /**
     * 基于MetadataReader的方式去构建
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

        // 如果必要的话, 先setBeanClass, 避免出问题...后续再完善
        if (metadataReader is SimpleMetadataReader) {
            this.setBeanClass(metadataReader.classLoader.loadClass(metadataReader.annotationMetadata.getClassName()))
        }
    }


    override fun getMetadata() = metadata ?: throw IllegalStateException("AnnotationMetadata is null")
}