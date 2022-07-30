package com.wanna.framework.beans.factory

import com.wanna.framework.context.exception.BeansException

open class BeanDefinitionStoreException(beanName: String, message: String, cause: Throwable? = null) :
    BeansException(message, cause, beanName)