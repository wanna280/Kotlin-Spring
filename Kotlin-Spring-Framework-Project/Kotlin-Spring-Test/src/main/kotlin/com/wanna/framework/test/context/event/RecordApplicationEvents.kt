package com.wanna.framework.test.context.event

import java.lang.annotation.Inherited

/**
 * 标注在测试类上，表示Test方法执行过程当中，需要记录一下Spring ApplicationContext发布的事件到[ApplicationEvents]当中来
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Inherited
annotation class RecordApplicationEvents()
