package com.wanna.framework.context.util

import com.wanna.framework.beans.BeanDefinition
import com.wanna.framework.context.BeanMethod
import com.wanna.framework.context.ImportBeanDefinitionRegistrar

/**
 * 这是对一个配置类的封装
 */
class ConfigurationClass(_beanDefinition: BeanDefinition) {

    // beanName
    val beanName: String = _beanDefinition.beanName

    // clazz
    val configurationClass: Class<*> = _beanDefinition.beanClass

    // beanMethods
    val beanMethods = HashSet<BeanMethod>()

    // importBeanDefinitionRegistrars
    val importBeanDefinitionRegistrars = HashMap<ImportBeanDefinitionRegistrar, String>()

    fun addBeanMethod(beanMethod: BeanMethod) {
        beanMethods.add(beanMethod)
    }

    fun addRegistrar(registrar: ImportBeanDefinitionRegistrar) {
        importBeanDefinitionRegistrars[registrar] = "wanna"
    }

    override fun hashCode(): Int {
        return configurationClass.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        // 如果configurationClass匹配的话，那么return true
        return if (other != null && other is ConfigurationClass) other.configurationClass == configurationClass else false
    }
}