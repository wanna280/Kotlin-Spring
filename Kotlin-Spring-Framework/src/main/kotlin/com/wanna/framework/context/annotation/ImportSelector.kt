package com.wanna.framework.context.annotation

import com.wanna.framework.core.type.AnnotationMetadata
import java.util.function.Predicate

/**
 * ImportSelector，可以给容器中导入组件
 */
interface ImportSelector {
    /**
     * 设置排除的Filter
     */
    fun getExclusionFilter(): Predicate<String>? {
        return null
    }

    fun selectImports(metadata: AnnotationMetadata): Array<String>
}