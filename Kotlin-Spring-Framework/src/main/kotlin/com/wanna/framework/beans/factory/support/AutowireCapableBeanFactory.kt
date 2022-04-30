package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.TypeConverter

interface AutowireCapableBeanFactory : BeanFactory {

    companion object {
        const val AUTOWIRE_NO = 0
        const val AUTOWIRE_BY_TYPE = 1
        const val AUTOWIRE_BY_NAME = 2
        const val AUTOWIRE_CONSTRUCTOR = 3
    }

    /**
     * 根据依赖的描述信息(descriptor)，从容器中解析出来合适的依赖，可以是Map/List以及普通的Entity
     * @param descriptor 要进行解析的依赖信息，可以是一个方法/字段等
     * @param requestingBeanName 请求进行注入的beanName，例如A需要注入B，那么requestingBeanName=A，而descriptor中则维护了B中的相关信息
     * @return 如果从容器中解析到了合适的依赖，那么return 解析到的依赖；如果解析不到，return null
     */
    fun resolveDependency(descriptor: DependencyDescriptor, requestingBeanName: String): Any?

    /**
     * 根据依赖的描述信息(descriptor)，从容器中解析出来合适的依赖，可以是Map/List以及普通的Entity
     * @param descriptor 要进行解析的依赖信息，可以是一个方法/字段等
     * @param requestingBeanName 请求进行注入的beanName，例如A需要注入B，那么requestingBeanName=A，而descriptor中则维护了B中的相关信息
     * @param autowiredBeanName // TODO
     * @param typeConverter 类型转换器
     * @return 如果从容器中解析到了合适的依赖，那么return 解析到的依赖；如果解析不到，return null
     */
    fun resolveDependency(
        descriptor: DependencyDescriptor,
        requestingBeanName: String,
        autowiredBeanName: MutableSet<String>?,
        typeConverter: TypeConverter?
    ): Any?
}