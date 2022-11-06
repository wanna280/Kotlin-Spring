package com.wanna.framework.test.context.event.annotation

import com.wanna.framework.context.event.EventListener
import com.wanna.framework.test.context.event.BeforeTestMethodEvent

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@EventListener(classes = [BeforeTestMethodEvent::class])
annotation class BeforeTestMethod()
