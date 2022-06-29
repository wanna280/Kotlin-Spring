package com.wanna.framework.web.method.annotation

import com.wanna.framework.web.bind.WebDataBinder
import com.wanna.framework.web.bind.WebRequestDataBinder

/**
 * 它可以标注在一个方法上，用来对SpringWebMvc部分的WebDataBinder去完成初始化工作；
 * 在@InitBinder标注的方法上，支持去注入WebDataBinder，自己去对WebDataBinder去完成初始化工作；
 * 当然，@InitBinder的方法，也支持去注入SpringWebMvc相关的注解参数(Model/ModelAndView相关的参数不行，别的参数都允许去注入)；
 * 整体使用和一个@RequestMapping的方法类似，@InitBinder方法会在每次请求时，都将其去应用给当前请求的WebDataBinder
 *
 * @see WebDataBinder
 * @see WebRequestDataBinder
 */
@Target(AnnotationTarget.FUNCTION)
annotation class InitBinder()
