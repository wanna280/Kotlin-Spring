package com.wanna.framework.beans.factory.config

/**
 * Bean的引用，暂时设置为beanName，后期支持去进行自动解析
 *
 * @param beanName beanName
 */
open class RuntimeBeanReference(private val beanName: String) : BeanReference {

    private var source: Any? = null

    override fun getBeanName() = beanName

    override fun getSource(): Any? = source

    open fun setSource(source:Any?) {
        this.source = source
    }
}