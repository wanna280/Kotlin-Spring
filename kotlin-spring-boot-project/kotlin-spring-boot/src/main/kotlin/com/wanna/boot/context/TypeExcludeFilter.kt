package com.wanna.boot.context

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.core.type.classreading.MetadataReader
import com.wanna.framework.core.type.classreading.MetadataReaderFactory
import com.wanna.framework.core.type.filter.TypeFilter

/**
 * 它支持拿出Spring BeanFactory中所有的TypeExcludeFilter去完成过滤
 *
 * @see TypeFilter
 */
open class TypeExcludeFilter : TypeFilter, BeanFactoryAware {

    /**
     * BeanFactory
     */
    private var beanFactory: BeanFactory? = null

    /**
     * delegate TypeExcludeFilters, 从BeanFactory当中去进行获取
     */
    private var delegates: Collection<TypeExcludeFilter>? = null

    /**
     * set BeanFactory
     */
    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    /**
     * 获取出BeanFactory当中的所有的TypeExcludeFilter, 对给定的clazz去进行匹配
     *
     * @param clazz 待匹配的clazz
     * @return 如果匹配成功(return true), 那么该Bean就需要从BeanFactory当中remove掉
     */
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

    override fun matches(metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory): Boolean {
        // TODO
        return false
    }

    /**
     * 从BeanFactory当中拿出来所有的[TypeFilter]
     *
     * @param beanFactory BeanFactory
     * @return TypeExcludeFilters
     */
    private fun getDelegates(beanFactory: ListableBeanFactory): Collection<TypeExcludeFilter> {
        if (this.delegates == null) {
            this.delegates = beanFactory.getBeansForType(TypeExcludeFilter::class.java).values
        }
        return this.delegates!!
    }
}