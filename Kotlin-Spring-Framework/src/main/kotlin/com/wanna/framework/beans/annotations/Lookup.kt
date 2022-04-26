package com.wanna.framework.beans.annotations


@Target(AnnotationTarget.FUNCTION)
annotation class Lookup(val value: String = "")
