package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.core.io.DescriptiveResource
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.classreading.MetadataReader
import com.wanna.framework.lang.Nullable

/**
 * 这是对一个配置类的封装, 一个配置类当中, 往往会记录它导入的ImportSource、BeanMethod、ImportBeanDefinitionRegistrar等信息
 *
 * @param metadata 配置类的注解元信息
 * @param resource 配置类的资源
 * @param beanName beanName
 */
open class ConfigurationClass(val metadata: AnnotationMetadata, var resource: Resource, var beanName: String?) {
    /**
     * beanMethods
     */
    val beanMethods = LinkedHashSet<BeanMethod>()

    /**
     * 被哪个组件导入进来的?
     */
    private val importedBy = LinkedHashSet<ConfigurationClass>()

    /**
     * importSources
     */
    val importedSources = LinkedHashMap<String, Class<out BeanDefinitionReader>>()

    /**
     * importBeanDefinitionRegistrars
     */
    private val importBeanDefinitionRegistrars = LinkedHashMap<ImportBeanDefinitionRegistrar, AnnotationMetadata>()

    /**
     * 基于clazz和beanName去进行构建
     *
     * @param clazz Class
     * @param beanName beanName
     */
    constructor(clazz: Class<*>, beanName: String?) : this(
        AnnotationMetadata.introspect(clazz), DescriptiveResource(clazz.name), beanName
    )

    /**
     * 基于Class和importedBy去进行构建
     *
     * @param clazz Class
     * @param importedBy importedBy
     */
    constructor(clazz: Class<*>, importedBy: ConfigurationClass?) : this(clazz, beanName = null) {
        if (importedBy != null) {
            this.importedBy.add(importedBy)
        }
    }

    /**
     * 基于AnnotationMetadata和beanName去进行构建
     *
     * @param metadata AnnotationMetadata
     * @param beanName beanName
     */
    constructor(metadata: AnnotationMetadata, beanName: String) : this(
        metadata, DescriptiveResource(metadata.getClassName()), beanName
    )

    /**
     * 基于AnnotationMetadata和beanName去进行构建
     *
     * @param metadataReader MetadataReader
     * @param beanName beanName
     */
    constructor(metadataReader: MetadataReader, beanName: String?) : this(
        metadataReader.annotationMetadata, metadataReader.resource, beanName
    )

    /**
     * 基于MetadataReader和importedBy去进行构建
     *
     * @param metadataReader MetadataReader
     * @param importedBy importedBy
     */
    constructor(metadataReader: MetadataReader, @Nullable importedBy: ConfigurationClass?) : this(
        metadataReader, beanName = null
    ) {
        if (importedBy != null) {
            this.importedBy.add(importedBy)
        }
    }

    constructor(beanDefinition: BeanDefinition, beanName: String?) : this(beanDefinition.getBeanClass()!!, beanName)


    /**
     * 往配置类当中添加一个@Bean的方法
     *
     * @param beanMethod 添加一个@Bean标注的方法
     */
    open fun addBeanMethod(beanMethod: BeanMethod) {
        beanMethods += beanMethod
    }

    /**
     * 获取已经注册的ImportBeanDefinitionRegistrars
     *
     * @return ImportBeanDefinitionRegistrar Map(key-registrar,value导入该ImportBeanDefinitionRegistrar的配置类的注解元信息)
     */
    open fun getImportBeanDefinitionRegistrars(): Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> {
        return importBeanDefinitionRegistrars
    }

    /**
     * 当前配置类当中是否有@Bean标注的方法?
     *
     * @return 如果有@Bean方法, return true; 否则return false
     */
    open fun hasBeanMethod(): Boolean = beanMethods.isNotEmpty()

    /**
     * 当前配置类是否有导入ImportBeanDefinitionRegistrar
     *
     * @return 如果存在有ImportBeanDefinitionRegistrar的话, 那么return true; 否则return false
     */
    open fun hasRegistrar(): Boolean = this.importBeanDefinitionRegistrars.isNotEmpty()

    /**
     * 添加一个ImportBeanDefinitionRegistrar到当前ConfigurationClass当中来
     *
     * @param registrar ImportBeanDefinitionRegistrar
     * @param annotationMetadata 注解源信息
     */
    open fun addRegistrar(registrar: ImportBeanDefinitionRegistrar, annotationMetadata: AnnotationMetadata) {
        importBeanDefinitionRegistrars[registrar] = annotationMetadata
    }

    /**
     * 设置当前配置类是否哪个配置了类导入的?
     *
     * @param configurationClass 导入当前配置类的配置类
     */
    open fun setImportedBy(configurationClass: ConfigurationClass) {
        importedBy += configurationClass
    }

    /**
     * 获取当前配置类是被哪些配置类所导入?
     */
    open fun getImportedBy(): Collection<ConfigurationClass> = importedBy

    /**
     * 是否被Import进来的?
     *
     * @return 如果当前的配置类是被导入的, return true; 不然return false
     */
    open fun isImportedBy(): Boolean = importedBy.isNotEmpty()

    /**
     * 添加ImportSource, 通过@ImportSource注解导入的resource, 并将其使用的BeanDefinitionReader去进行注册和保存
     *
     * @param reader readerClass
     * @param resource resourceLocation
     */
    open fun addImportSource(resource: String, reader: Class<out BeanDefinitionReader>) {
        importedSources[resource] = reader
    }

    override fun hashCode() = metadata.getClassName().hashCode()

    override fun equals(other: Any?): Boolean {
        // 如果configurationClassName匹配的话, 那么return true
        return if (other != null && other is ConfigurationClass) other.metadata.getClassName() == metadata.getClassName() else false
    }

    override fun toString(): String = "ConfigurationClass: beanName='$beanName', resource= $resource"
}