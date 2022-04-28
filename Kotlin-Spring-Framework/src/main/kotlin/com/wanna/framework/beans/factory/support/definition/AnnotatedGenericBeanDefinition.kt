package com.wanna.framework.beans.factory.support.definition

/**
 * 这是一个被注解标注的通用的BeanDefinition
 */
open class AnnotatedGenericBeanDefinition(_beanClass: Class<*>?) : AnnotatedBeanDefinition,
    GenericBeanDefinition(_beanClass) {

}