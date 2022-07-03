package com.wanna.framework.web.bind.annotation

/**
 * 标识这是一个ResponseBody，方法的返回值，将以ResponseBody的方式去进行写出，它会被RequestResponseBodyMethodProcessor所处理；
 * 它可以标注在Controller的类上，也可以标注在Controller的HandlerMethod(@RequestMapping标注的方法)上
 *
 * @see RequestResponseBodyMethodProcessor
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class ResponseBody
