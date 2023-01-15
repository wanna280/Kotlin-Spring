package com.wanna.framework.beans.factory

import com.wanna.framework.context.exception.NoSuchBeanDefinitionException
import com.wanna.framework.context.exception.NoUniqueBeanDefinitionException
import com.wanna.framework.lang.Nullable

/**
 * Spring的BeanFactory, 提供SpringBean的管理;
 * 在这个接口当中, 主要提供一些关于Spring Bean的获取的功能.
 */
interface BeanFactory {
    companion object {

        /**
         * FactoryBean的前缀常量
         *
         * @see FactoryBean
         */
        const val FACTORY_BEAN_PREFIX = "&"
    }

    /**
     * 通过name去容器当中去获取Bean
     *
     * @param beanName beanName
     * @return 根据name获取到的Bean
     * @throws NoSuchBeanDefinitionException 如果没有找到合适的Bean的话
     */
    @Throws(NoSuchBeanDefinitionException::class)
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
    fun <T : Any> getBean(beanName: String, type: Class<T>): T

    /**
     * 通过type去进行获取Bean
     *
     * @param type 想要去获取Bean的type
     * @return 根据Bean获取到的Bean
     * @throws NoSuchBeanDefinitionException 如果找不到合适的Bean
     * @throws NoUniqueBeanDefinitionException 如果根据type找到的Bean不唯一的话
     */
    @Throws(NoSuchBeanDefinitionException::class, NoUniqueBeanDefinitionException::class)
    fun <T : Any> getBean(type: Class<T>): T

    /**
     * 检查当前BeanFactory当中是否包含有这样的beanName的Bean?
     *
     * @param name name
     * @return 如果BeanFactory当中包含有这样的beanName的话Bean的话, 那么return true; 如果不包含的话, 那么return false
     */
    fun containsBean(name: String): Boolean

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
     * beanName对应的Bean的类型是否匹配type？
     *
     * @param name 要匹配的beanName
     * @param type 要去进行匹配的类型
     * @return 根据name获取到的Bean是否匹配指定的类型？
     *  @throws NoSuchBeanDefinitionException 如果beanFactory当中没有这样beanName的BeanDefinition的话¬
     */
    @Throws(NoSuchBeanDefinitionException::class)
    fun isTypeMatch(name: String, type: Class<*>): Boolean

    /**
     * 根据beanName去匹配beanType
     *
     * @param beanName beanName
     * @throws NoSuchBeanDefinitionException 如果beanFactory当中没有这样beanName的BeanDefinition的话
     */
    @Throws(NoSuchBeanDefinitionException::class)
    @Nullable
    fun getType(beanName: String): Class<*>?
}