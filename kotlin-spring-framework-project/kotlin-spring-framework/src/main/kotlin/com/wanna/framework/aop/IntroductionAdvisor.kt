package com.wanna.framework.aop

// TODO
interface IntroductionAdvisor : Advisor {
    fun getClassFilter() : ClassFilter
}