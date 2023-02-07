package com.wanna.boot.autoconfigure.condition

import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.context.annotation.Condition
import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.core.type.AnnotatedTypeMetadata
import com.wanna.framework.core.type.ClassMetadata
import com.wanna.framework.core.type.MethodMetadata
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils

/**
 * SpringBoot的[Condition]的基类, 它基于[Condition]去进行匹配, 并提供了匹配的结果的日志输出以及诊断的相关功能;
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
abstract class SpringBootCondition : Condition {

    /**
     * Logger
     */
    protected val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 实现[Condition]的matches方法, 去完成配置类的匹配的模板代码的实现
     *
     * @param context 封装BeanDefinition/Environment/BeanFactory/ClassLoader等信息的Context
     * @return 如果匹配成功, 那么return true; 如果匹配失败, 则return false
     */
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val classOrMethodName = getClassOrMethodName(metadata)

        // 交给子类去完成获取Condition的匹配结果
        val outcome = getConditionOutcome(context, metadata)

        // log输出匹配的相关信息
        logOutcome(classOrMethodName, outcome)

        // 记录匹配结果的相关信息到ConditionEvaluationReport当中
        recordEvaluation(context, classOrMethodName, outcome)
        return outcome.match
    }

    /**
     * 获取Condition的匹配结果, 是一个抽象的模板方法, 交给子类去进行实现
     *
     * @param context ConditionContext
     * @param metadata 配置类的注解元信息Metadata
     * @return 针对给定的配置类的元信息去进行匹配的结果
     */
    abstract fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome

    /**
     * 将对当前配置类的匹配的结果去进行日志的输出, 采用trace的方式去进行输出
     *
     * @param classOrMethodName 匹配的类/方法
     * @param outcome 针对该类/方法去进行匹配的结果
     */
    protected open fun logOutcome(classOrMethodName: String, outcome: ConditionOutcome) {
        if (logger.isTraceEnabled) {
            logger.trace(getLogMessage(classOrMethodName, outcome))
        }
    }

    /**
     * 根据给定的[AnnotatedTypeMetadata], 去计算得到方法名/类名
     *
     * @param metadata 方法/类的注解元信息
     * @return 计算得到的类名/方法名
     */
    private fun getName(metadata: AnnotatedTypeMetadata): String {
        if (metadata is ClassMetadata) {
            return metadata.getClassName()
        }
        if (metadata is MethodMetadata) {
            return metadata.getDeclaringClassName() + '.' + metadata.getMethodName()
        }
        return metadata.toString()
    }

    /**
     * 根据给定的[AnnotatedTypeMetadata], 去计算得到方法名/类名
     *
     * @param metadata 方法/类的注解元信息
     * @return 从元信息当中去提取到的方法名/类名
     */
    private fun getClassOrMethodName(metadata: AnnotatedTypeMetadata): String {
        if (metadata is ClassMetadata) {
            return metadata.getClassName()
        }
        if (metadata is MethodMetadata) {
            return metadata.getDeclaringClassName() + "#" + metadata.getMethodName()
        }
        throw IllegalStateException("Metadata is not a ClassMetadata or MethodMetadata")
    }

    /**
     * 记录一条配置类的匹配的结果到[ConditionEvaluationReport]当中
     *
     * @param context context
     * @param classOrMethodName 匹配的类名/方法名
     * @param outcome 匹配类/方法的注解源信息的结果
     */
    private fun recordEvaluation(context: ConditionContext, classOrMethodName: String, outcome: ConditionOutcome) {
        if (context.getBeanFactory() != null) {
            ConditionEvaluationReport.get(context.getBeanFactory()!!)
                .recordConditionEvaluation(classOrMethodName, this, outcome)
        }
    }

    /**
     * 获取用于去对最终的匹配结果, 去进行日志输出的Message消息
     *
     * @param classOrMethodName 匹配的类/方法
     * @param outcome 针对该类/方法去进行匹配的结果
     * @return 进行日志输出的Message消息
     */
    private fun getLogMessage(classOrMethodName: String, outcome: ConditionOutcome): String {
        val builder = StringBuilder()
        builder.append("Condition ")
        builder.append(ClassUtils.getShortName(javaClass))
        builder.append(" on ")
        builder.append(classOrMethodName)
        builder.append(if (outcome.match) " matched" else " did not match")
        if (StringUtils.hasText(outcome.getMessageString())) {
            builder.append(" due to")
            builder.append(outcome.getMessageString())
        }
        return builder.toString()
    }
}