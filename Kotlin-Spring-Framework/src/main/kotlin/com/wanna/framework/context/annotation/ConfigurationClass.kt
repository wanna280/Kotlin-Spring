package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是对一个配置类的封装，一个配置类当中，往往会记录它导入的ImportSource、BeanMethod、ImportBeanDefinitionRegistrar等信息
 *
 * @param _clazz 目标配置类
 * @param _beanName beanName(可以为null，后期去进行生成)
 */
open class ConfigurationClass(_clazz: Class<*>, _beanName: String?) {
    constructor(_clazz: Class<*>) : this(_clazz, null)
    constructor(beanDefinition: BeanDefinition, beanName: String?) : this(beanDefinition.getBeanClass()!!, beanName)

    // 该配置类的注解元信息
    val metadata: AnnotationMetadata = AnnotationMetadata.introspect(_clazz)

    // clazz
    val configurationClass: Class<*> = _clazz

    var beanName: String? = _beanName

    // beanMethods
    val beanMethods = LinkedHashSet<BeanMethod>()

    // 被哪个组件导入进来的？
    private val importedBy = LinkedHashSet<ConfigurationClass>()

    // importSources
    val importedSources = LinkedHashMap<String, Class<out BeanDefinitionReader>>()

    // importBeanDefinitionRegistrars
    private val importBeanDefinitionRegistrars = LinkedHashMap<ImportBeanDefinitionRegistrar, AnnotationMetadata>()

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
     * 当前配置类当中是否有@Bean标注的方法？
     */
    open fun hasBeanMethod(): Boolean = beanMethods.isNotEmpty()

    /**
     * 当前配置类是否有导入ImportBeanDefinitionRegistrar
     */
    open fun hasRegistrar(): Boolean = beanMethods.isNotEmpty()

    open fun addRegistrar(registrar: ImportBeanDefinitionRegistrar, annotationMetadata: AnnotationMetadata) {
        importBeanDefinitionRegistrars[registrar] = annotationMetadata
    }

    /**
     * 设置当前配置类是否哪个配置了类导入的？
     *
     * @param configurationClass 导入当前配置类的配置类
     */
    open fun setImportedBy(configurationClass: ConfigurationClass) {
        importedBy += configurationClass
    }

    /**
     * 获取当前配置类是被哪些配置类所导入？
     */
    open fun getImportedBy(): Collection<ConfigurationClass> = importedBy

    /**
     * 是否被Import进来的？
     *
     * @return 如果当前的配置类是被导入的，return true；不然return false
     */
    open fun isImportedBy(): Boolean = importedBy.isNotEmpty()

    /**
     * 添加ImportSource，通过@ImportSource注解导入的resource，并将其使用的BeanDefinitionReader去进行注册和保存
     *
     * @param reader readerClass
     * @param resource resourceLocation
     */
    open fun addImportSource(resource: String, reader: Class<out BeanDefinitionReader>) {
        importedSources[resource] = reader
    }

    override fun hashCode() = configurationClass.hashCode()

    override fun equals(other: Any?): Boolean {
        // 如果configurationClass匹配的话，那么return true
        return if (other != null && other is ConfigurationClass) other.configurationClass == configurationClass else false
    }

    override fun toString(): String {
        return "ConfigurationClass($beanName, $configurationClass)"
    }


}