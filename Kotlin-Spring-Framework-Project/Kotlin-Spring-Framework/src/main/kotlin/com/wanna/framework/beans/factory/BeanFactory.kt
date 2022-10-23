package com.wanna.framework.beans.factory

import com.wanna.framework.context.exception.NoSuchBeanDefinitionException
import com.wanna.framework.context.exception.NoUniqueBeanDefinitionException
import com.wanna.framework.context.processor.beans.BeanPostProcessor

/**
 * Spring的BeanFactory，提供SpringBean的管理
 */
interface BeanFactory {
    companion object {
        const val FACTORY_BEAN_PREFIX = "&"  // FactoryBean的前缀，static final变量
    }

    /**
     * 通过name去容器当中去获取Bean
     *
     * @param beanName beanName
     * @return 根据name获取到的Bean
     * @throws NoSuchBeanDefinitionException 如果没有找到合适的Bean的话
     */
    fun getBean(beanName: String): Any

    /**
     * 通过name去容器中getBean
     *
     * @param beanName beanName
     * @param args 明确(explicit)指定创建Bean时应该使用的参数(创建一个新的实例时才能生效)
     * @return 根据name获取到的Bean
     * @throws NoSuchBeanDefinitionException 如果没有找到合适的Bean的话
     */
    @Throws(NoSuchBeanDefinitionException::class)
    fun getBean(beanName: String, vararg args: Any?): Any

    /**
     * 通过name和type去进行获取Bean
     *
     * @param beanName beanName
     * @param type beanType
     * @return 根据name获取到的Bean
     * @throws NoSuchBeanDefinitionException 如果找不到合适的Bean
     */
    @Throws(NoSuchBeanDefinitionException::class)
    fun <T> getBean(beanName: String, type: Class<T>): T

    /**
     * 通过type去进行获取Bean
     *
     * @param type 想要去获取Bean的type
     * @return 根据Bean获取到的Bean
     * @throws NoSuchBeanDefinitionException 如果找不到合适的Bean
     * @throws NoUniqueBeanDefinitionException 如果根据type找到的Bean不唯一的话
     */
    @Throws(NoSuchBeanDefinitionException::class, NoUniqueBeanDefinitionException::class)
    fun <T> getBean(type: Class<T>): T

    /**
     * 根据beanName去判断该Bean是否是单例Bean？
     *
     * @param beanName beanName
     * @return 该Bean是否是单例的？
     */
    @Throws(NoSuchBeanDefinitionException::class)
    fun isSingleton(beanName: String): Boolean

    /**
     * 根据beanName去判断该Bean是否是原型的？
     *
     * @param beanName beanName
     * @return 该Bean是否是原型的？
     */
    @Throws(NoSuchBeanDefinitionException::class)
    fun isPrototype(beanName: String): Boolean

    /**
     * 添加BeanPostProcessor
     *
     * @param processor 你想要往BeanFactory当中添加的BeanPostProcessor
     */
    fun addBeanPostProcessor(processor: BeanPostProcessor)

    /**
     * 根据type移除BeanPostProcessor
     *
     * @param type 要去移除的BeanPostProcessor的类型
     */
    fun removeBeanPostProcessor(type: Class<*>)

    /**
     * 根据index去移除BeanPostProcessor
     *
     * @param index 具体的index
     */
    fun removeBeanPostProcessor(index: Int)

    /**
     * 根据beanName去判断该Bean是否是一个FactoryBean
     *
     * @param name beanName
     * @return 该Bean是否是一个FactoryBean
     */
    fun isFactoryBean(name: String): Boolean

    /**
     * beanName对应的Bean的类型是否匹配type？
     *
     * @param name 要匹配的beanName
     * @param type 要去进行匹配的类型
     * @return 根据name获取到的Bean是否匹配指定的类型？
     */
    fun isTypeMatch(name: String, type: Class<*>): Boolean

    /**
     * 根据beanName去匹配beanType
     *
     * @param beanName beanName
     */
    fun getType(beanName: String): Class<*>?
}