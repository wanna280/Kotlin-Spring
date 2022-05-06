package com.wanna.framework.context.weaving

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.context.ConfigurableApplicationContext.Companion.LOAD_TIME_WEAVER_BEAN_NAME
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.instrument.classloading.LoadTimeWeaver

/**
 * 这是处理LoadTimeWeaverAware的处理器，负责给Bean注入LoadTimeAware对象
 *
 * @see LoadTimeWeaver
 * @see LoadTimeWeaverAware
 */
open class LoadTimeWeaverAwareProcessor(private var beanFactory: BeanFactory?) : BeanPostProcessor, BeanFactoryAware {

    // 无参数构造器，设置beanFactory为null
    constructor() : this(null)

    private var loadTimeWeaver: LoadTimeWeaver? = null

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    /**
     * 完成LoadTimeWeaverAware的处理，去完成LoadTimeWeaver的注入
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