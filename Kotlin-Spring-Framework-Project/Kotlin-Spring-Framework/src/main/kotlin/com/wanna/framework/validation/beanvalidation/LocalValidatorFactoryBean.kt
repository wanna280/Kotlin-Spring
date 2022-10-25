package com.wanna.framework.validation.beanvalidation

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ApplicationContextAware
import javax.validation.ValidatorFactory

/**
 * 基于本地的ValidatorFactory去实现的SpringValidatorAdapter
 *
 * @see SpringValidatorAdapter
 */
open class LocalValidatorFactoryBean : SpringValidatorAdapter(), InitializingBean, ApplicationContextAware {

    /**
     * ValidatorFactory
     */
    private var validatorFactory: ValidatorFactory? = null

    /**
     * ApplicationContext
     */
    private var applicationContext: ApplicationContext? = null

    override fun afterPropertiesSet() {

    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}