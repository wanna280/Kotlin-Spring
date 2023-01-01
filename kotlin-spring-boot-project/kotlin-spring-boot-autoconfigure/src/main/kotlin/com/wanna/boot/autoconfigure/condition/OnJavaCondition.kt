package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.core.type.AnnotatedTypeMetadata

/**
 * 提供Java版本的匹配功能，只有当版本匹配时才需要去进行自动装配
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/27
 */
open class OnJavaCondition : SpringBootCondition() {

    companion object {
        /**
         * 获取当前JVM的版本
         *
         * @see JavaVersion
         */
        @JvmStatic
        private val JVM_VERSION = JavaVersion.getJavaVersion()
    }


    /**
     * 对Java版本去进行匹配
     *
     * @param context ConditionContext
     * @param metadata 注解元信息
     * @return 匹配Java版本的结果
     */
    override fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        val attributes = metadata.getAnnotations().get(ConditionalOnJava::class.java)
        if (!attributes.present) {
            throw IllegalStateException("无法找到@ConditionalOnJava注解")
        }
        val range = attributes.getEnum("range", ConditionalOnJava.Range::class.java)
        val javaVersion = attributes.getEnum(MergedAnnotation.VALUE, JavaVersion::class.java)
        return getMatchOutcome(range, JVM_VERSION, javaVersion)
    }

    /**
     * 获取到匹配结果
     *
     * @param range 匹配方式(">="/"<")
     */
    protected open fun getMatchOutcome(
        range: ConditionalOnJava.Range,
        runningVersion: JavaVersion,
        version: JavaVersion
    ): ConditionOutcome {
        val result = when (range) {
            ConditionalOnJava.Range.EQUAL_OR_NEWER -> runningVersion.isEqualOrNewerThan(version)
            ConditionalOnJava.Range.OLDER_THAN -> runningVersion.isOlderThan(version)
        }
        val message =
            "需要的是Java版本[${range.rangeName}${version.versionName}], 实际是[${runningVersion.versionName}]"
        return if (result) ConditionOutcome.match(message)
        else ConditionOutcome.noMatch("Java版本不匹配！$message")
    }
}