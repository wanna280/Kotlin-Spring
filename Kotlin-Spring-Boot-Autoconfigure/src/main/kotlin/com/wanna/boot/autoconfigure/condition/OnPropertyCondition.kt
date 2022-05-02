package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.ConditionContext
import com.wanna.framework.core.type.AnnotatedTypeMetadata

open class OnPropertyCondition : SpringBootCondition() {
    override fun getConditionOutcome(context: ConditionContext, metadata: AnnotatedTypeMetadata): ConditionOutcome {
        return ConditionOutcome.match()
    }
}