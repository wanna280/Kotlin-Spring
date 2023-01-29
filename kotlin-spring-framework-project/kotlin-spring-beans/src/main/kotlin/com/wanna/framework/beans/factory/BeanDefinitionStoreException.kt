package com.wanna.framework.beans.factory

import com.wanna.framework.beans.BeansException

open class BeanDefinitionStoreException(val beanName: String, message: String, cause: Throwable? = null) :
    BeansException(message, cause) {

}