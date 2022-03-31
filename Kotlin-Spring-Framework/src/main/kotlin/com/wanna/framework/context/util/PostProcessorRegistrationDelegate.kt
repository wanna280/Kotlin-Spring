package com.wanna.framework.context.util

import com.wanna.framework.context.AbstractApplicationContext
import com.wanna.framework.context.ConfigurableListableBeanFactory
import com.wanna.framework.context.processor.factory.BeanDefinitionRegistryPostProcessor
import com.wanna.framework.context.processor.factory.BeanFactoryPostProcessor

/**
 * 这是一个执行PostProcessor的委托类
 */
class PostProcessorRegistrationDelegate {

    companion object {

        /**
         * 执行BeanFactoryPostProcessors
         *
         * @param postProcessors 在ApplicationContext当中已经注册的BeanFactoryPostProcessor列表
         * @param beanFactory BeanFactory
         */
        @JvmStatic
        fun invokeBeanFactoryPostProcessors(
            beanFactory: ConfigurableListableBeanFactory, postProcessors: List<BeanFactoryPostProcessor>
        ) {
            postProcessors.forEach {
                if (it is BeanDefinitionRegistryPostProcessor) {
                    it.postProcessBeanDefinitionRegistry(beanFactory)
                }
            }
        }

        /**
         * 注册所有的BeanPostProcessor
         */
        @JvmStatic
        fun registerBeanPostProcessors(
            beanFactory: ConfigurableListableBeanFactory,
            applicationContext: AbstractApplicationContext
        ) {

        }
    }
}