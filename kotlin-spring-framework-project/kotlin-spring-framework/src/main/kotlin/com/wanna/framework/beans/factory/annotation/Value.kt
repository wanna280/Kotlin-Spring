package com.wanna.framework.beans.factory.annotation

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FIELD,AnnotationTarget.VALUE_PARAMETER)
annotation class Value(val value: String)
