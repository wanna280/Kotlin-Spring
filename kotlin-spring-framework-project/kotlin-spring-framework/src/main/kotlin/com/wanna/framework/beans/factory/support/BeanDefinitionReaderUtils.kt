package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.AbstractBeanDefinition
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.util.ClassUtils

/**
 * BeanDefinitionReader的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
object BeanDefinitionReaderUtils {

    /**
     * 注册一个BeanDefinition到BeanDefinitionRegistry当中
     *
     * @param beanDefinitionHolder BeanDefinition&BeanName
     * @param registry BeanDefinitionRegistry
     */
    @JvmStatic
    fun registerBeanDefinition(beanDefinitionHolder: BeanDefinitionHolder, registry: BeanDefinitionRegistry) {
        registry.registerBeanDefinition(beanDefinitionHolder.beanName, beanDefinitionHolder.beanDefinition)
    }

    /**
     * 根据className去创建一个BeanDefinition
     *
     * @param parentName parentBeanDefinitionName
     * @param classLoader classLoader
     * @param className className
     * @throws ClassNotFoundException 如果出现了类找不到的情况
     * @return 创建出来的BeanDefinition
     */
    @Throws(ClassNotFoundException::class)
    @JvmStatic
    fun createBeanDefinition(
        parentName: String?,
        className: String?,
        classLoader: ClassLoader?
    ): AbstractBeanDefinition {
        val beanDefinition = GenericBeanDefinition()
        beanDefinition.setParent(parentName)
        if (className != null) {
            if (classLoader != null) {
                val clazz = ClassUtils.forName<Any>(className, classLoader)
                beanDefinition.setBeanClass(clazz)
            } else {
               // TODO
            }
        }
        return beanDefinition
    }
}