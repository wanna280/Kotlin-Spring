package com.wanna.framework.context

/**
 * ImportSelector，可以给容器中导入组件
 */
interface ImportSelector {
    fun selectImports(): Array<String>
}