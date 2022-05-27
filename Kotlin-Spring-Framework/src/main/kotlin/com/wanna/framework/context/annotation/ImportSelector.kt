package com.wanna.framework.context.annotation

import com.wanna.framework.core.type.AnnotationMetadata
import java.util.function.Predicate

/**
 * ImportSelector，可以给容器中导入组件；
 */
interface ImportSelector {
    /**
     * 设置排除的Filter，符合Filter规范的组件，将会被排除掉
     */
    fun getExclusionFilter(): Predicate<String>? {
        return null
    }

    /**
     * 给容器当中导入组件
     *
     * @param metadata 导入ImportSelector的注解信息
     * @return 要给容器中导入组件的className列表
     */
    fun selectImports(metadata: AnnotationMetadata): Array<String>
}