package com.wanna.framework.beans.factory.annotation

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FIELD)
annotation class Value(val value: String)
