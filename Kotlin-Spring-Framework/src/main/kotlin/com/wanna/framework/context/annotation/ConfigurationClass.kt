package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.StandardAnnotationMetadata

/**
 * 这是对一个配置类的封装
 */
open class ConfigurationClass(_clazz: Class<*>, _beanName: String?) {

    constructor(beanDefinition: BeanDefinition, beanName: String?) : this(beanDefinition.getBeanClass()!!, beanName)

    val metadata: AnnotationMetadata = StandardAnnotationMetadata(_clazz)

    // clazz
    val configurationClass: Class<*> = _clazz

    val beanName: String? = _beanName

    // beanMethods
    val beanMethods = HashSet<BeanMethod>()

    // 被哪个组件导入进来的？
    private val importedBy = HashSet<ConfigurationClass>()

    // importSources
    val importedSources = HashMap<String, Class<out BeanDefinitionReader>>()

    // importBeanDefinitionRegistrars
    val importBeanDefinitionRegistrars = HashMap<ImportBeanDefinitionRegistrar, AnnotationMetadata>()

    fun addBeanMethod(beanMethod: BeanMethod) {
        beanMethods += beanMethod
    }

    /**
     * 获取已经注册的ImportBeanDefinitionRegistrars
     */
    fun getImportBeanDefinitionRegistrars(): Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> {
        return importBeanDefinitionRegistrars
    }

    fun hasBeanMethod(): Boolean {
        return beanMethods.isNotEmpty()
    }

    fun hasRegistrar() = beanMethods.isNotEmpty()

    fun addRegistrar(registrar: ImportBeanDefinitionRegistrar, annotationMetadata: AnnotationMetadata) {
        importBeanDefinitionRegistrars[registrar] = annotationMetadata
    }

    fun setImportedBy(configurationClass: ConfigurationClass) {
        importedBy += configurationClass
    }

    /**
     * 获取被哪些组件所导入？
     */
    fun getImportedBy() = importedBy

    /**
     * 是否被Import进来的？
     */
    fun isImportedBy() = importedBy.isNotEmpty()

    fun addImportSource(resource: String, reader: Class<out BeanDefinitionReader>) {
        importedSources[resource] = reader
    }

    override fun hashCode() = configurationClass.hashCode()


    override fun equals(other: Any?): Boolean {
        // 如果configurationClass匹配的话，那么return true
        return if (other != null && other is ConfigurationClass) other.configurationClass == configurationClass else false
    }
}