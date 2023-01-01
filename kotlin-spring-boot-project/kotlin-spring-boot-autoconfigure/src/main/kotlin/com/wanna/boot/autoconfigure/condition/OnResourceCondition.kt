package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.core.type.AnnotatedTypeMetadata

/**
 * 提供对资源去进行匹配的[SpringBootCondition]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/27
 *
 * @see ConditionalOnResource
 */
open class OnResourceCondition : SpringBootCondition() {

    @Suppress("UNCHECKED_CAST")
    override fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        val resourceLoader = context.getResourceLoader()
        val annotation = metadata.getAnnotations().get(ConditionalOnResource::class.java)
        if (!annotation.present) {
            throw IllegalStateException("无法找到@ConditionalOnResource注解")
        }
        val resources = annotation.getStringArray("resources")
        if (resources.isEmpty()) {
            throw IllegalStateException("@ConditionalOnResource注解的resource属性当中必须指定至少一个资源文件")
        }

        // 统计出来所有的缺失的资源文件列表...
        val missing = ArrayList<String>()
        resources.forEach {
            val location = context.getEnvironment().resolvePlaceholders(it)!!
            if (!resourceLoader.getResource(location).exists()) {
                missing += it
            }
        }
        if (missing.isNotEmpty()) {
            return ConditionOutcome.noMatch(ConditionMessage.of("无法找到给定的这些资源文件：$missing"))
        }
        return ConditionOutcome.match("对于给定的这些资源文件全部都找到了[$resources]")
    }
}