package com.wanna.framework.beans

/**
 * 提供对于[PropertyAccessor]的创建的工厂方法的门面
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/14
 */
object PropertyAccessorFactory {

    /**
     * 为给定的Java对象去创建一个[BeanWrapper], 提供JavaBeans风格的属性值访问
     *
     * @param obj 要去进行包装和访问的对象
     * @return 提供对于目标对象的访问的PropertyAccessor
     * @see BeanWrapperImpl
     */
    @JvmStatic
    fun forBeanPropertyAccess(obj: Any): BeanWrapper {
        return BeanWrapperImpl(obj)
    }
}