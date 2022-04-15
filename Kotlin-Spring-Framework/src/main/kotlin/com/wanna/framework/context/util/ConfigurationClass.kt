package com.wanna.framework.context.util

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.BeanMethod
import com.wanna.framework.context.ImportBeanDefinitionRegistrar
import com.wanna.framework.context.annotations.BeanDefinitionReader
import com.wanna.main

/**
 * 这是对一个配置类的封装
 */
class ConfigurationClass(_clazz: Class<*>) {

    constructor(beanDefinition: BeanDefinition) : this(beanDefinition.getBeanClass()!!)

    // clazz
    val configurationClass: Class<*> = _clazz

    // beanMethods
    val beanMethods = HashSet<BeanMethod>()

    // 被哪个组件导入进来的？
    private val importedBy = HashSet<ConfigurationClass>()

    // importSources
    val importedSources = HashMap<String, Class<out BeanDefinitionReader>>()

    // importBeanDefinitionRegistrars
    val importBeanDefinitionRegistrars = HashMap<ImportBeanDefinitionRegistrar, String>()

    fun addBeanMethod(beanMethod: BeanMethod) {
        beanMethods += beanMethod
    }

    fun hasBeanMethod(): Boolean {
        return beanMethods.isNotEmpty()
    }

    fun hasRegistrar(): Boolean {
        return beanMethods.isNotEmpty()
    }

    fun addRegistrar(registrar: ImportBeanDefinitionRegistrar) {
        importBeanDefinitionRegistrars[registrar] = "wanna"
    }

    fun setImportedBy(configurationClass: ConfigurationClass) {
        importedBy += configurationClass
    }

    fun isImportedBy(): Boolean {
        return importedBy.isNotEmpty()
    }

    fun addImportSource(resource: String, reader: Class<out BeanDefinitionReader>) {
        importedSources[resource] = reader
    }

    override fun hashCode(): Int {
        return configurationClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        // 如果configurationClass匹配的话，那么return true
        return if (other != null && other is ConfigurationClass) other.configurationClass == configurationClass else false
    }
}