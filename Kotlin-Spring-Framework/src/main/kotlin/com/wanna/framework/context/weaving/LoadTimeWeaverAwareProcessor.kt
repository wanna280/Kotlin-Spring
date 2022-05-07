package com.wanna.framework.context.weaving

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.ConfigurableApplicationContext.Companion.LOAD_TIME_WEAVER_BEAN_NAME
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.instrument.classloading.LoadTimeWeaver

/**
 * 这是处理LoadTimeWeaverAware的处理器，它是一个BeanPostProcessor，负责给Bean处理LoadTimeWeaverAware接口，去注入LoadTimeWeaver对象
 *
 * @see LoadTimeWeaver
 * @see LoadTimeWeaverAware
 */
open class LoadTimeWeaverAwareProcessor(private var beanFactory: BeanFactory?) : BeanPostProcessor, BeanFactoryAware {

    // 提供一个无参数的辅助构造器，并去设置beanFactory为null
    constructor() : this(null)

    private var loadTimeWeaver: LoadTimeWeaver? = null  // LoadTimeWeaver

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    /**
     * 完成LoadTimeWeaverAware接口的处理，去对Bean完成LoadTimeWeaver的注入
     */
    override fun postProcessBeforeInitialization(beanName: String, bean: Any): Any? {
        if (bean is LoadTimeWeaverAware) {
            var weaver = loadTimeWeaver
            if (weaver == null) {
                weaver = beanFactory!!.getBean(LOAD_TIME_WEAVER_BEAN_NAME, LoadTimeWeaver::class.java)
            }
            bean.setLoadTimeWeaver(weaver!!)
        }
        return super.postProcessBeforeInitialization(beanName, bean)
    }
}