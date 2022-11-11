package com.wanna.boot.context

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.core.type.filter.TypeFilter

/**
 * 它会拿出容器中所有的TypeExcludeFilter去完成过滤
 */
open class TypeExcludeFilter : TypeFilter, BeanFactoryAware {

    private var beanFactory: BeanFactory? = null

    private var delegates: Collection<TypeExcludeFilter>? = null

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun matches(clazz: Class<*>?): Boolean {
        if (beanFactory is ListableBeanFactory && javaClass == TypeExcludeFilter::class.java) {
            getDelegates(beanFactory as ListableBeanFactory).forEach {
                if (it.matches(clazz)) {
                    return true
                }
            }
        }
        return false
    }

    private fun getDelegates(beanFactory: ListableBeanFactory): Collection<TypeFilter> {
        if (this.delegates == null) {
            this.delegates = beanFactory.getBeansForType(TypeExcludeFilter::class.java).values
        }
        return this.delegates!!
    }
}