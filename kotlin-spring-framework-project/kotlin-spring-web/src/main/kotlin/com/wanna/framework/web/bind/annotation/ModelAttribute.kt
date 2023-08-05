package com.wanna.framework.web.bind.annotation

/**
 * 对于@ModelAttribute注解标注的方法, 在该方法参数当中, 可以去使用SpringWebMvc当中的全部参数, 比如@RequestParam/@RequestHeader;
 * 当时也支持使用Model/Map的方式去注入ModelMap数据去操作Model数据; 对于ModelAttribute方法的返回值, 也会被自动放入到Model数据当中;
 * 当指定了name时, Model的key就是name, 不然为返回值类型的首字母小写, 而Model数据的value则为方法的返回值
 *
 * * 1.可以标注在一个ControllerAdvice的方法上, 它将会apply给所有Controller的所有Handler方法
 * * 2.可以标注在一个Controller的非@RequestMapping方法上, 它将会应用给该Controller的所有Handler方法
 * * 3.可以标注在一个Controller的@RequestMapping方法上, 支持使用返回值解析器去处理该Handler方法(也是将返回值放入到Model数据当中)
 * * 4.当然, 也可以可以标注在上面几种情况的方法参数上, 标识想要获取ModelAttribute的JavaBean(从RequestParam当中去封装成为JavaBean)
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE_PARAMETER)
annotation class ModelAttribute(val name: String = "")
