package com.wanna.framework.beans.factory

/**
 * 可以支持列举的BeanFactory, 可以提供批量获取Bean等方式的支持
 *
 * @see BeanFactory
 * @see com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
 */
interface ListableBeanFactory : BeanFactory {
    /**
     * 根据type去匹配容器中所有该类型的Bean的beanName
     *
     * Note: 允许去使用非单例的Bean(includeNonSingletons=true), 并且允许eager去进行加载(allowEagerInit=true),
     * ！！！请不要提前使用这个方法, 避免出现Bean的先后顺序出现严重的问题
     *
     * @param type 要去进行匹配的类型
     * @return List for beanNames
     */
    fun getBeanNamesForType(type: Class<*>): List<String>

    /**
     * 根据type去匹配容器中所有该类型的Bean的beanName
     *
     * @param type 要去进行匹配的类型
     * @param includeNonSingletons 是否包含非单例的Bean
     * @param allowEagerInit 是否eagerInit
     */
    fun getBeanNamesForType(type: Class<*>, includeNonSingletons: Boolean, allowEagerInit: Boolean): List<String>

    /**
     * 给定具体类型type, 去容器中找到该类型的所有Bean列表
     *
     * Note: 允许去使用非单例的Bean(includeNonSingletons=true), 并且允许eager去进行加载(allowEagerInit=true),
     * ！！！请不要提前使用这个方法, 避免出现Bean的先后顺序出现严重的问题
     *
     * @param type 要去进行匹配的类型
     */
    fun <T : Any> getBeansForType(type: Class<T>): Map<String, T>

    /**
     * 根据type去匹配容器以及父容器当中所有该类型的Bean的beanName
     *
     * Note: 允许去使用非单例的Bean(includeNonSingletons=true), 并且允许eager去进行加载(allowEagerInit=true),
     * ！！！请不要提前使用这个方法, 避免出现Bean的先后顺序出现严重的问题
     *
     * @param type 要去进行匹配的类型
     * @return List for beanNames
     */
    fun getBeanNamesForTypeIncludingAncestors(type: Class<*>): List<String>

    /**
     * 根据type去匹配容器以及父容器当中所有该类型的Bean的beanName
     *
     * @param type 要去进行匹配的类型
     * @param includeNonSingletons 是否包含非单例的Bean
     * @param allowEagerInit 是否eagerInit
     * @return List for beanNames
     */
    fun getBeanNamesForTypeIncludingAncestors(
        type: Class<*>, includeNonSingletons: Boolean, allowEagerInit: Boolean
    ): List<String>

    /**
     * 给定具体类型type, 去容器以及父容器中找到该类型的所有Bean列表
     *
     * Note: 允许去使用非单例的Bean(includeNonSingletons=true), 并且允许eager去进行加载(allowEagerInit=true),
     * ！！！请不要提前使用这个方法, 避免出现Bean的先后顺序出现严重的问题
     *
     * @param type 要去进行匹配的类型
     * @return key-beanName, value beanObject
     */
    fun <T : Any> getBeansForTypeIncludingAncestors(type: Class<T>): Map<String, T>

    /**
     * 获取当前容器中的BeanDefinition的数量
     *
     * @return BeanDefinition的数量
     */
    fun getBeanDefinitionCount(): Int

    /**
     * 容器中是否包含这样的BeanDefinition? 不会匹配注册的单例Bean
     *
     * @param name beanName
     * @return 如果beanFactory当中有, return true, 否则return false
     */
    fun containsBeanDefinition(name: String): Boolean

    /**
     * 获取所有的BeanDefinition的name列表
     *
     * @return BeanDefinitionNames列表
     */
    fun getBeanDefinitionNames(): List<String>
}