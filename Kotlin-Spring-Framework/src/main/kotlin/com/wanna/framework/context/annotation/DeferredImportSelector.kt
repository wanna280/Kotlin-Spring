package com.wanna.framework.context.annotation

/**
 * 这是一个延时进行导入的ImportSelector，会在用户自定义的组件全部被扫描完成之后，才进行组件的导入
 */
interface DeferredImportSelector : ImportSelector {

}