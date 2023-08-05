package com.wanna.framework.scheduling.annotation

import com.wanna.framework.context.annotation.ImportSelector
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 为异步提供配置的Selector, 负责给容器中导入异步相关的组件
 */
open class AsyncConfigurationSelector : ImportSelector {
    override fun selectImports(metadata: AnnotationMetadata): Array<String> {
        return arrayOf(ProxyAsyncConfiguration::class.java.name)
    }
}