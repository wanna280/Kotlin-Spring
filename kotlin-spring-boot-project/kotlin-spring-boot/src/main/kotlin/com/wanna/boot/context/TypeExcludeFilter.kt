package com.wanna.boot.context

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.core.type.classreading.MetadataReader
import com.wanna.framework.core.type.classreading.MetadataReaderFactory
import com.wanna.framework.core.type.filter.TypeFilter
import com.wanna.framework.lang.Nullable

/**
 * 直接从Spring BeanFactory中获取到所有的[TypeExcludeFilter]去完成过滤
 *
 * @see TypeFilter
 */
open class TypeExcludeFilter : TypeFilter, BeanFactoryAware {

    /**
     * BeanFactory
     */
    @Nullable
    private var beanFactory: BeanFactory? = null

    /**
     * delegate TypeExcludeFilters, 从BeanFactory当中去进行获取
     */
    @Nullable
    private var delegates: Collection<TypeExcludeFilter>? = null

    /**
     * set BeanFactory
     */
    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    /**
     * 获取到BeanFactory当中的所有的TypeExcludeFilter, 对当前类去执行匹配
     *
     * @param metadataReader 读取类的相关信息的MetadataReader
     * @param metadataReaderFactory MetadataReaderFactory
     * @return 如果它被BeanFactory当中的TypeExcludeFilter匹配上return true, 否则return false
     */
    override fun matches(metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory): Boolean {
        val beanFactory = beanFactory ?: return false
        if (beanFactory !is ListableBeanFactory || javaClass != TypeExcludeFilter::class.java) {
            return false
        }
        for (filter in getDelegates(beanFactory)) {
            if (filter.matches(metadataReader, metadataReaderFactory)) {
                return true
            }
        }
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