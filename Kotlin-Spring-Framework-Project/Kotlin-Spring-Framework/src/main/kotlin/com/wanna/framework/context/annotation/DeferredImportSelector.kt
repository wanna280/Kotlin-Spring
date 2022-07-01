package com.wanna.framework.context.annotation

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
    fun getGroup(): Class<out Group>? {
        return null
    }

    interface Group {

    }
}