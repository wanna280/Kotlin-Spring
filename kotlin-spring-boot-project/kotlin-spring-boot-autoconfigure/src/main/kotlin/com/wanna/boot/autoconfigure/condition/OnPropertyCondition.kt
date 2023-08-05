package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.type.AnnotatedTypeMetadata

/**
 * 匹配Spring应用的[Environment]当中的属性值是否成立的[SpringBootCondition]
 *
 * @see ConditionalOnProperty
 */
open class OnPropertyCondition : SpringBootCondition() {

    /**
     * 执行对于Spring应用的[Environment]当中的属性值, 和注解上的配置信息, 去进行匹配
     *
     * @param context context
     * @param metadata 类/方法的注解元信息
     * @return 对于该类/方法的注解元信息的匹配结果
     */
    override fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        // TODO
        return ConditionOutcome.match()
    }
}