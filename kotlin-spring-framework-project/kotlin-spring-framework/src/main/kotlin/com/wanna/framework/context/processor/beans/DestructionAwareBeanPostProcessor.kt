package com.wanna.framework.context.processor.beans

import com.wanna.framework.beans.factory.support.DisposableBean

/**
 * 它是一个Spring的BeanPostProcessor的子接口, 可以给针对指定的Bean去注册去注册before-destruction的回调(callback);
 * 对于Spring提供的DisposableBean, 也支持在destroy时去进行自动回调, 其作用也大致相同
 *
 * @see DisposableBean
 */
interface DestructionAwareBeanPostProcessor : BeanPostProcessor {

    /**
     * 在摧毁一个容器当中的Bean时, 应该执行的相关收尾工作的回调;
     * 它通常会作用于单例的Bean和ScopedBean(prototypeBean不在这个范围内)
     *
     * @param bean bean
     * @param beanName beanName
     */
    fun postProcessBeforeDestruction(bean: Any, beanName: String) {

    }

    /**
     * 判断一个Bean在被destroy时, 是否需要被当前的BeanPostProcessor去进行回调处理
     *
     * @return return true时会应用postProcessBeforeDestruction方法; return false则直接pass掉
     */
    fun requiresDestruction(bean: Any): Boolean {
        return true
    }
}