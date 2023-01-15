package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.Condition
import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.core.type.AnnotatedTypeMetadata

/**
 * 标识这是一个SpringBoot的Condition, 也是SpringBoot当中的所有的Condition的基类, 它基于Condition并提供了日志的输出功能;
 * 提供了日志的输出, 目的是为了去告诉用户哪些配置类已经被自动装配进容器当中, 哪些配置类没有被装配进容器当中
 */
abstract class SpringBootCondition : Condition {
    /**
     * 实现Condition, 去完成配置类的匹配
     *
     * @param context BeanDefinition/Environment/BeanFactory/ClassLoader
     */
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        // 交给子类去完成获取Condition的匹配结果
        val outcome = getConditionOutcome(context, metadata)
        logOutcome("" + outcome, outcome)
        return outcome.match
    }

    /**
     * 获取Condition的匹配结果, 是一个抽象的模板方法, 交给子类去进行实现
     *
     * @param context ConditionContext
     * @param metadata 配置类的Metadata
     */
    abstract fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome

    /**
     * 将对当前配置类的匹配的结果去进行日志的输出
     *
     * @see ConditionOutcome
     */
    protected open fun logOutcome(classOrMethodName: String?, outcome: ConditionOutcome) {

    }
}