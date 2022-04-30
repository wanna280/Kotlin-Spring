package com.wanna.framework.beans.factory.annotation


@Target(AnnotationTarget.FUNCTION)
annotation class Lookup(val value: String = "")
