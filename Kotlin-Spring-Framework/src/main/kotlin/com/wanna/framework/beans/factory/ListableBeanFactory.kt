package com.wanna.framework.beans.factory

interface ListableBeanFactory : BeanFactory {
    /**
     * 根据type去匹配容器中所有该类型的Bean的beanName
     *
     * @param type 要去进行匹配的类型
     */
    fun getBeanNamesForType(type: Class<*>): List<String>

    /**
     * 根据type去匹配容器中所有该类型的Bean的beanName
     *
     * @param type 要去进行匹配的类型
     */
    fun getBeanNamesForType(type: Class<*>, includeNonSingletons: Boolean, allowEagerInit: Boolean) : List<String>

    /**
     * 给定具体类型type，去容器中找到该类型的所有Bean列表
     *
     * @param type 要去进行匹配的类型
     */
    fun <T> getBeansForType(type: Class<T>): Map<String, T>

    /**
     * 根据type去匹配容器以及父容器当中所有该类型的Bean的beanName
     *
     * @param type 要去进行匹配的类型
     */
    fun getBeanNamesForTypeIncludingAncestors(type: Class<*>): List<String>

    /**
     * 给定具体类型type，去容器以及父容器中找到该类型的所有Bean列表
     *
     * @param type 要去进行匹配的类型
     */
    fun <T> getBeansForTypeIncludingAncestors(type: Class<T>): Map<String, T>

    /**
     * 获取当前容器中的BeanDefinition的数量
     */
    fun getBeanDefinitionCount(): Int

    /**
     * 容器中是否包含这样的BeanDefinition？
     */
    fun containsBeanDefinition(name: String): Boolean

    /**
     * 获取所有的BeanDefinition的name列表
     */
    fun getBeanDefinitionNames(): List<String>
}