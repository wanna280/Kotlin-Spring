package com.wanna.framework.beans.definition

/**
 * 这是在扫描的过程中创建的beanDefinition，扫描过程中拿到的BeanDefinition，本身一定是因为标注了注解被扫描进来的，因此它一定是AnnotatedBeanDefinition
 */
open class ScannedGenericBeanDefinition(_beanClass:Class<*>) : AnnotatedBeanDefinition, GenericBeanDefinition(_beanClass) {

}