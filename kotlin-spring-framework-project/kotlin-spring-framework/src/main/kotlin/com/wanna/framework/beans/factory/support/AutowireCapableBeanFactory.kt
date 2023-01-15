package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.TypeConverter
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.lang.Nullable

/**
 * 在原本的BeanFactory功能的基础上, 新增了拥有自动装配能力
 *
 * @see BeanFactory
 * @see AbstractAutowireCapableBeanFactory
 */
interface AutowireCapableBeanFactory : BeanFactory {

    companion object {

        /**
         * 自动注入的模式, NO代表不去进行注入
         */
        const val AUTOWIRE_NO = 0

        /**
         * 自动注入的模式, BY_TYPE代表将会对所有字段去进行按照类型去进行注入
         */
        const val AUTOWIRE_BY_TYPE = 1

        /**
         * 自动注入的模式, BY_NAME代表会对所有的字段去按照beanName去进行注入
         */
        const val AUTOWIRE_BY_NAME = 2

        /**
         * 自动注入模式, 在创建时基于构造器去进行注入
         */
        const val AUTOWIRE_CONSTRUCTOR = 3

        /**
         * "origin instance"的后缀, 在初始化一个已经存在的Bean时, 会采用"{className}.ORIGIN"的方式去进行生成;
         * 目的是为了快速返回实例对象, 不去经历代理的阶段
         *
         * @see initializeBean
         * @see applyBeanPostProcessorsBeforeInitialization
         * @see applyBeanPostProcessorsAfterInitialization
         */
        const val ORIGINAL_INSTANCE_SUFFIX = ".ORIGIN"
    }

    /**
     * 根据依赖的描述信息(descriptor), 从容器中解析出来合适的依赖, 可以是Map/List以及普通的Entity
     *
     * @param descriptor 要进行解析的依赖信息, 可以是一个方法/字段等
     * @param requestingBeanName 请求进行注入的beanName, 例如A需要注入B, 那么requestingBeanName=A, 而descriptor中则维护了B中的相关信息
     * @return 如果从容器中解析到了合适的依赖, 那么return 解析到的依赖; 如果解析不到, return null
     */
    @Nullable
    fun resolveDependency(descriptor: DependencyDescriptor, @Nullable requestingBeanName: String?): Any?

    /**
     * 根据依赖的描述信息(descriptor), 从容器中解析出来合适的依赖去进行注入, 可以是Map/List以及普通的Entity
     *
     * @param descriptor 要进行解析的依赖信息, 可以是一个方法/字段等
     * @param requestingBeanName 请求进行注入的beanName, 例如A需要注入B, 那么requestingBeanName=A, 而descriptor中则维护了B中的相关信息
     * @param autowiredBeanNames autowiredBeanNames
     * @param typeConverter 类型转换器, 提供对于类型的转换(可以为null, 将会使用默认的TypeConverter)
     * @return 如果从BeanFactory中解析到了合适的依赖, 那么return 解析到的依赖实例化对象; 如果解析不到, return null
     */
    @Nullable
    fun resolveDependency(
        descriptor: DependencyDescriptor,
        @Nullable requestingBeanName: String?,
        @Nullable autowiredBeanNames: MutableSet<String>?,
        @Nullable typeConverter: TypeConverter?
    ): Any?

    /**
     * 对外提供对Bean去完成BeforeInitialization工作, 让外部也能通过BeanFactory去执行这个回调
     *
     * @param existingBean bean object
     * @param beanName beanName
     * @return 经过BeforeInitialization之后得到的Bean
     */
    fun applyBeanPostProcessorsBeforeInitialization(existingBean: Any, beanName: String): Any

    /**
     * 对外提供对Bean去完成AfterInitialization工作, 让外部也能通过BeanFactory去执行这个回调
     *
     * @param existingBean bean object
     * @param beanName beanName
     * @return 经过AfterInitialization之后得到的Bean
     */
    fun applyBeanPostProcessorsAfterInitialization(existingBean: Any, beanName: String): Any

    /**
     * 对一个Bean去应用属性自动注入(Note:供BeanFactory外部去进行使用)
     *
     * @param existingBean 需要去进行自动注入的Bean
     * @param autowireMode autowireMode
     * @param dependencyCheck dependencyCheck
     */
    fun autowireBeanProperties(existingBean: Any, autowireMode: Int, dependencyCheck: Boolean)

    /**
     * 初始化一个Bean, 供beanFactory外部去进行使用, 完成一个Bean的初始化工作;
     * 在初始化Bean时, 会自动执行beforeInitialization/afterInitialization方法, 也会执行Aware的接口;
     *
     * @param existingBean bean
     * @param beanName beanName
     */
    fun initializeBean(existingBean: Any, beanName: String)

    /**
     * 给定具体的class, 使用当前的BeanFactory去进行创建一个Bean
     *
     * @param clazz beanClass
     * @param T beanType
     * @return 创建好的Bean实例对象
     */
    fun <T> createBean(clazz: Class<T>): T

    /**
     * 摧毁一个Bean, 供beanFactory外部去进行使用
     *
     * @param existingBean 要进行摧毁的已经存在的单例Bean
     */
    fun destroy(existingBean: Any)
}