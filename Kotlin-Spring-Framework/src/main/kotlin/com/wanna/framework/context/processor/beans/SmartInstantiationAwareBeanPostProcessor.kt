package com.wanna.framework.context.processor.beans

interface SmartInstantiationAwareBeanPostProcessor : InstantiationAwareBeanPostProcessor {

    /**
     * 获取Bean的早期引用
     */
    fun getEarlyReference(bean: Any, beanName: String): Any {
        return bean
    }

}