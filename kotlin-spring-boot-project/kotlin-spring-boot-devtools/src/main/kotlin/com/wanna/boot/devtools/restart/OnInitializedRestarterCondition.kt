package com.wanna.boot.devtools.restart

import com.wanna.boot.autoconfigure.condition.ConditionOutcome
import com.wanna.boot.autoconfigure.condition.SpringBootCondition
import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.core.type.AnnotatedTypeMetadata

/**
 * 去匹配Restarter是否已经完成初始化工作的Condition
 *
 * @see ConditionalOnInitializedRestarter
 */
class OnInitializedRestarterCondition : SpringBootCondition() {
    override fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        val restarter = Restarter.getInstance() ?: return ConditionOutcome.noMatch("Restarter还没完成创建")
        restarter.getInitialUrls() ?: return ConditionOutcome.noMatch("Restarter的InitialUrls为空")
        return ConditionOutcome.match("Restarter已经完成创建, 并且Restarter的InitialUrls已经完成初始化")
    }
}