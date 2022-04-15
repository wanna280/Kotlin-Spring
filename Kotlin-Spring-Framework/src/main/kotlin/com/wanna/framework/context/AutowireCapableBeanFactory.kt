package com.wanna.framework.context

interface AutowireCapableBeanFactory : BeanFactory {

    companion object {
        const val AUTOWIRE_NO = 0
        const val AUTOWIRE_BY_TYPE = 1
        const val AUTOWIRE_BY_NAME = 2
        const val AUTOWIRE_CONSTRUCTOR = 3
    }

}