package com.wanna.boot.autoconfigure.condition

import com.wanna.boot.autoconfigure.AutoConfigurationMetadata
import com.wanna.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.MVC
import com.wanna.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.core.type.AnnotatedTypeMetadata
import com.wanna.framework.util.ClassUtils

/**
 * WebApplication的Condition匹配
 *
 * @see ConditionalOnWebApplication
 */
open class OnWebApplicationCondition : FilteringSpringBootCondition() {

    companion object {
        /**
         * Servlet的标识类
         */
        private const val SERVLET_MARKER = "javax.servlet.Servlet"

        /**
         * Netty的标识类
         */
        private const val NETTY_MARKER = "io.netty.bootstrap.ServerBootstrap"
    }

    override fun getOutcomes(
        autoConfigurationClasses: Array<String?>, autoConfigurationMetadata: AutoConfigurationMetadata
    ): Array<ConditionOutcome?> {
        return emptyArray()
    }

    override fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        if (metadata.isAnnotated(ConditionalOnWebApplication::class.java.name)) {
            val onWeb = metadata.getAnnotations().get(ConditionalOnWebApplication::class.java)
            val type = onWeb.getEnum("type", ConditionalOnWebApplication.Type::class.java)
            return if (isServlet(context) && type == SERVLET) {
                ConditionOutcome.match()
            } else if (isMvc(context) && type == MVC) {
                ConditionOutcome.match()
            } else {
                ConditionOutcome.noMatch()
            }
        }
        return ConditionOutcome.match()
    }

    /**
     * 检查当前是否是Mvc环境
     *
     * @param context context
     * @return 如果是Mvc环境, 那么return true
     */
    private fun isMvc(context: ConditionContext): Boolean {
        return ClassUtils.isPresent(NETTY_MARKER, context.getClassLoader())
    }

    /**
     * 检查当前是否是Servlet环境?
     *
     * @param context context
     * @return 如果是Servlet环境, return true
     */
    private fun isServlet(context: ConditionContext): Boolean {
        return ClassUtils.isPresent(SERVLET_MARKER, context.getClassLoader())
    }
}