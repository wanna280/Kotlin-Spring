package com.wanna.framework.context.processor.beans

/**
 * 它是一个BeanPostProcessor的子接口
 *
 * 这是一个给摧毁Bean去注册回调方法的BeanPostProcessor，它可以给一个Bean去注册before-destruction的回调(callback)
 */
interface DestructionAwareBeanPostProcessor : BeanPostProcessor {

    /**
     * 在摧毁一个容器当中的Bean时，应该执行的相关收尾工作的回调；它通常会作用于单例的Bean和ScopedBean(prototypeBean不在这个范围内)
     *
     * @param bean bean
     * @param beanName beanName
     */
    fun postProcessBeforeDestruction(bean: Any, beanName: String) {

    }

    /**
     * 判断一个Bean在被destroy时，是否需要被当前的BeanPostProcessor去进行回调
     *
     * @return return true时会应用postProcessBeforeDestruction方法；return false则直接pass掉
     */
    fun requiresDestruction(bean: Any): Boolean {
        return true
    }
}