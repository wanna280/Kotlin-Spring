package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.context.processor.beans.DestructionAwareBeanPostProcessor
import com.wanna.framework.core.util.ReflectionUtils
import com.wanna.framework.core.util.StringUtils
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

/**
 * 它是一个DisposableBean的适配器，目的是将DisposableBean能够适配Runnable；
 * 因为Scope的Bean注册的回调必须得是一个Runnable，因此在这里去进行适配Runnable；
 *
 * 在Spring当中，对于使用了任何一种destroy方法的Bean，都会被包装成为一样一个DisposableBeanAdapter，并注册到Spring BeanFactory当中；
 * 当Spring BeanFactory执行了destroyBean时，会自动回调这个类当中的destroy方法；
 *
 * 不管是@PreDestroy注解的方法，还是它实现了DisposableBean，还是设置了destroyMethod到BeanDefinition当中，还是实现了JDK当中的AutoCloseable接口；
 * 这几种方式，都会被Spring BeanFactory统一包装成为一个DisposableBeanAdapter，注册到Spring BeanFactory当中
 *
 * @see DisposableBean
 * @see DefaultListableBeanFactory.registerDisposableBeanIfNecessary
 */
open class DisposableBeanAdapter(
    private val bean: Any,
    private val beanName: String,
    private val mbd: RootBeanDefinition?,
    _postProcessors: List<DestructionAwareBeanPostProcessor>
) : DisposableBean, Runnable {

    // 是否需要执行DisposableBean？
    private val invokeDisposableBean: Boolean = bean is DisposableBean

    // 要应用给当前Bean的DestructionAwareBeanPostProcessor列表
    private val beanPostProcessors = filterPostProcessors(_postProcessors, bean)

    // destroy方法
    private var destroyMethod: Method? = null

    // destroyMethodName
    private var destroyMethodName: String? = null

    init {
        val destroyMethodName = inferDestroyMethodIfNecessary(bean, mbd)

        // 如果destroyMethodName并不是来自于DisposableBean的destroy方法
        if (destroyMethodName != null && !("destroy" == destroyMethodName && this.invokeDisposableBean)) {
            this.destroyMethodName = destroyMethodName
            var destroyMethod: Method? = null
            try {
                destroyMethod = bean::class.java.getMethod(destroyMethodName)
            } catch (ex: Exception) {

            }
            if (destroyMethod != null) {
                if (destroyMethod.parameterCount >= 1) {
                    throw IllegalStateException("destroy方法不支持多个参数")
                }
                this.destroyMethod = destroyMethod
            }
        }
    }

    /**
     * 执行destroy方法
     */
    override fun destroy() {
        // 1.应用所有的BeanPostProcessor，去进行回调
        if (this.beanPostProcessors.isNotEmpty()) {
            this.beanPostProcessors.forEach { it.postProcessBeforeDestruction(bean, beanName) }
        }

        // 2.如果要执行invokeDisposableBean的话
        if (this.invokeDisposableBean) {
            try {
                (this.bean as DisposableBean).destroy()
            } catch (ex: Throwable) {
                // 执行destroyMethod失败，打印日志信息
                logger.warn("执行DisposableBean的destroy方法失败，原因是[$ex]")
            }
        }

        // 3.如果有destroy方法，那么去执行该方法
        val destroyMethod = this.destroyMethod
        if (destroyMethod != null) {
            ReflectionUtils.makeAccessible(destroyMethod)
            try {
                ReflectionUtils.invokeMethod(destroyMethod, this.bean)
            } catch (ex: Throwable) {
                logger.warn("执行[beanName=$bean, beanClass=${this.bean::class.java}]的destroy方法[name=${destroyMethod.name}]失败，原因是[$ex]")
            }
        }
    }

    /**
     * 将destroy方法适配到Runnable，桥接去执行destroy方法
     */
    override fun run() {
        destroy()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DisposableBeanAdapter::class.java)

        /**
         * 过滤出来所有的能应用给当前Bean的DestructionAwareBeanPostProcessor
         *
         * @param postProcessors 候选的DestructionAwareBeanPostProcessor列表
         * @param bean 要匹配的Bean
         * @return 过滤之后的DestructionAwareBeanPostProcessor列表，全部都是能应用给当前Bean的Processor
         */
        private fun filterPostProcessors(
            postProcessors: List<DestructionAwareBeanPostProcessor>, bean: Any
        ): List<DestructionAwareBeanPostProcessor> {
            return postProcessors.filter { it.requiresDestruction(bean) }.toList()
        }

        /**
         * 判断一个Bean是否有Destory方法，DisposableBean/AutoCloseable，则return true
         *
         * @param bean bean
         * @param mbd MergedBeanDefinition
         * @return 是否有destroy方法？
         */
        @JvmStatic
        fun hasDestroyMethod(bean: Any, mbd: RootBeanDefinition): Boolean {
            if (bean is DisposableBean || bean is AutoCloseable) {
                return true
            }
            // 尝试去进行推断Destory方法
            return inferDestroyMethodIfNecessary(bean, mbd) != null
        }

        /**
         * 去推断是否有destroy方法？如果有返回找到的destroyMethodName，没有则return null
         *
         * @param bean bean
         * @param mbd MergedBeanDefinition
         * @return 如果找到了的话，返回值destroyMethodName，如果找不到的话，return null
         */
        private fun inferDestroyMethodIfNecessary(bean: Any, mbd: RootBeanDefinition?): String? {
            if (mbd == null) {
                return null
            }
            var destroyMethodName = mbd.resolvedDestroyMethodName
            if (destroyMethodName == null) {
                destroyMethodName = mbd.getDestoryMethodName()
                if (destroyMethodName == null && bean is AutoCloseable) {
                    if (bean !is DisposableBean) {
                        try {
                            destroyMethodName = bean::class.java.getMethod("close").name
                        } catch (ex: NoSuchMethodException) {
                            try {
                                destroyMethodName = bean::class.java.getMethod("shutdown").name
                            } catch (ex: NoSuchMethodException) {

                            }
                        }
                    }
                }
                mbd.resolvedDestroyMethodName = destroyMethodName ?: ""  // 如果为空时，设置为""，避免下次还进来去进行推断，直接从mbd当中获取即可
            }
            return if (StringUtils.hasText(destroyMethodName)) destroyMethodName else null
        }

        /**
         * 是否有可以去进行给当前Bean的DestructionAwareBeanPostProcessor？
         *
         * @param bean bean
         * @param postProcessors 要去进行匹配的DestructionAwareBeanPostProcessor列表
         * @return 是否有可以去进行应用的PostProcessor
         */
        fun hasApplicableProcessors(bean: Any, postProcessors: List<DestructionAwareBeanPostProcessor>): Boolean {
            postProcessors.forEach {
                if (it.requiresDestruction(bean)) {
                    return true
                }
            }
            return false
        }
    }
}