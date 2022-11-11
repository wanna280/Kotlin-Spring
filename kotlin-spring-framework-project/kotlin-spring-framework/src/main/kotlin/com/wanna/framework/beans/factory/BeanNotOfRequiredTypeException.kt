package com.wanna.framework.beans.factory

import com.wanna.framework.beans.BeansException

/**
 * 从Spring容器当中找到的Bean和你想要的Bean的类型之间不匹配
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/19
 */
open class BeanNotOfRequiredTypeException(beanName: String, val requiredType: Class<*>, val actualType: Class<*>) :
    BeansException("在从Spring BeanFactory当中使用beanName=[$beanName]获取Bean时, 获取到的对象类型为[${actualType.name}], 但是真实想要的类型为[${requiredType.name}]")