package com.wanna.framework.web.method.annotation

import com.wanna.framework.web.bind.RequestMethod

/**
 * 标识这是一个支持POST请求的RequestMapping
 */
@RequestMapping(method = [RequestMethod.POST])
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class PostMapping()
