package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.BeanDefinitionStoreException
import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * BeanDefinition发生覆盖的异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/8/6
 */
open class BeanDefinitionOverrideException(
    beanName: String,
    val beanDefinition: BeanDefinition,
    val existingBeanDefinition: BeanDefinition
) : BeanDefinitionStoreException(beanName, "") {

}