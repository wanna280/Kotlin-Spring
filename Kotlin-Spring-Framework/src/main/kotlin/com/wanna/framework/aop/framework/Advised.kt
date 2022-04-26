package com.wanna.framework.aop.framework

import com.wanna.framework.aop.Advice
import com.wanna.framework.aop.Advisor

interface Advised {

    fun getAdvisors() : Array<Advisor>

    fun addAdvisor(pos: Int, advisor: Advisor)

    fun addAdvisor(advisor: Advisor)

    fun addAdvice(advice: Advice)

    fun addAdvice(pos: Int, advice: Advice)
}