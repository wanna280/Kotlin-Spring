package com.wanna.framework.web.bind.annotation

import com.wanna.framework.web.method.annotation.RequestResponseBodyMethodProcessor

/**
 * 标识这是一个`ResponseBody`方法的返回值, 将以`ResponseBody`的方式去进行写出, 它会被[RequestResponseBodyMethodProcessor]所处理;
 * 它可以标注在Controller的类上, 也可以标注在Controller的HandlerMethod(例如`@RequestMapping`标注的方法)上
 *
 * @see RequestResponseBodyMethodProcessor
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class ResponseBody