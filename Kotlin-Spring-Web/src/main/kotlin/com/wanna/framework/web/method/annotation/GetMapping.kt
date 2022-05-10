package com.wanna.framework.web.method.annotation

import com.wanna.framework.web.bind.RequestMethod

/**
 * 标识这是一个支持GET请求的RequestMapping
 */
@RequestMapping(method = [RequestMethod.GET])
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class GetMapping()
