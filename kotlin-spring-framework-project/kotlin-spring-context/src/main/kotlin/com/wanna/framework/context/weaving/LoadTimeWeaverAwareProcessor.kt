package com.wanna.framework.context.weaving

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.ConfigurableApplicationContext.Companion.LOAD_TIME_WEAVER_BEAN_NAME
import com.wanna.framework.beans.factory.config.BeanPostProcessor
import com.wanna.framework.instrument.classloading.LoadTimeWeaver

/**
 * 处理[LoadTimeWeaverAware]的处理器, 它是一个[BeanPostProcessor], 负责给Bean处理[LoadTimeWeaverAware]接口, 去注入[LoadTimeWeaver]对象
 *
 * @see LoadTimeWeaver
 * @see LoadTimeWeaverAware
 */
open class LoadTimeWeaverAwareProcessor @JvmOverloads constructor(private var beanFactory: BeanFactory? = null) :
    BeanPostProcessor, BeanFactoryAware {

    /**
     * LoadTimeWeaver
     */
    private var loadTimeWeaver: LoadTimeWeaver? = null

    /**
     * 设置[BeanFactory]
     *
     * @param beanFactory BeanFactory
     */
    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    /**
     * 完成LoadTimeWeaverAware接口的处理, 去对Bean完成[LoadTimeWeaver]的注入
     *
     * @param bean bean
     * @param beanName beanName
     * @return 经过处理之后的Bean
     */
    override fun postProcessBeforeInitialization(beanName: String, bean: Any): Any {
        if (bean is LoadTimeWeaverAware) {
            var weaver = loadTimeWeaver
            if (weaver == null) {
                weaver = beanFactory!!.getBean(LOAD_TIME_WEAVER_BEAN_NAME, LoadTimeWeaver::class.java)
            }
            bean.setLoadTimeWeaver(weaver)
        }
        return bean
    }
}