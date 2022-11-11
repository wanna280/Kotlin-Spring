package com.wanna.framework.test.context.event.annotation

import com.wanna.framework.context.event.EventListener
import com.wanna.framework.test.context.event.PrepareTestInstanceEvent

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@EventListener(classes = [PrepareTestInstanceEvent::class])
annotation class PrepareTestInstance()
