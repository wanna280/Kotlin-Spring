package com.wanna.framework.context.annotation

import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是一个延时进行导入的ImportSelector，会在用户自定义的组件全部被扫描完成之后，才进行组件的导入
 *
 * @see ImportSelector
 * @see ImportBeanDefinitionRegistrar
 */
interface DeferredImportSelector : ImportSelector {

    /**
     * 返回这个ImportSelector应该所在的分组，如果分组为空(return null)，将会采用DeferredImportSelector的自身对象去进行分组
     *
     * @see Group
     * @see com.wanna.framework.context.annotation.ConfigurationClassParser.DeferredImportSelectorGroupingHandler
     * @see com.wanna.framework.context.annotation.ConfigurationClassParser.DeferredImportSelectorGrouping
     * @see com.wanna.framework.context.annotation.ConfigurationClassParser.deferredImportSelectorHandler
     */
    fun getGroup(): Class<out Group>? = null

    interface Group {

        /**
         * 对于给定的AnnotationMetadata和DeferredImportSelector去进行处理
         *
         * @param metadata AnnotationMetadata
         * @param selector DeferredImportSelector
         */
        fun process(metadata: AnnotationMetadata, selector: DeferredImportSelector)

        /**
         * 获取所有的要去进行导入的配置类信息
         *
         * @return Entries
         */
        fun selectImports(): Iterable<Entry>

        class Entry(val metadata: AnnotationMetadata, val importClassName: String) {

        }

    }
}