package com.wanna.framework.context.annotation

import com.wanna.framework.core.type.AnnotationMetadata

/**
 * ImportSelector，可以给容器中导入组件
 */
interface ImportSelector {
    fun selectImports(metadata: AnnotationMetadata): Array<String>
}