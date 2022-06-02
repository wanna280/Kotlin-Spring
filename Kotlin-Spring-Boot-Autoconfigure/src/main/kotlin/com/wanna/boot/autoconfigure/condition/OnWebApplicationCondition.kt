package com.wanna.boot.autoconfigure.condition

import com.wanna.boot.autoconfigure.AutoConfigurationMetadata
import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.core.type.AnnotatedTypeMetadata
import com.wanna.framework.core.util.ClassUtils

/**
 * WebApplication的Condition匹配
 *
 * @see ConditionalOnWebApplication
 */
open class OnWebApplicationCondition : FilteringSpringBootCondition() {

    companion object {
        const val MVC_WEB_MARKER = "com.wanna.framework.web.config.annotation.DelegatingWebMvcConfiguration"
    }

    override fun getOutcomes(
        autoConfigurationClasses: Array<String?>,
        autoConfigurationMetadata: AutoConfigurationMetadata
    ): Array<ConditionOutcome?> {
        TODO("Not yet implemented")
    }

    override fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        if (metadata.isAnnotated(ConditionalOnWebApplication::class.java.name)) {
            val onWeb = metadata.getAnnotationAttributes(ConditionalOnWebApplication::class.java)
            val type = onWeb["type"] as ConditionalOnWebApplication.Type
            if (isMvc(context) && type == ConditionalOnWebApplication.Type.MVC) {
                return ConditionOutcome.match()
            }
            if (!isMvc(context) && type == ConditionalOnWebApplication.Type.MVC) {
                return ConditionOutcome.noMatch()
            }
            return ConditionOutcome.match()
        }
        return ConditionOutcome.match()
    }

    private fun isMvc(context: ConditionContext): Boolean {
        if (ClassUtils.isPresent(MVC_WEB_MARKER, context.getClassLoader())) {
            return true
        }
        return false
    }
}