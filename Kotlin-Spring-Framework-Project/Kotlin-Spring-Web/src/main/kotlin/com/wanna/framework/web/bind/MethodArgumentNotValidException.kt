package com.wanna.framework.web.bind

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.validation.BindException
import com.wanna.framework.validation.BindingResult

/**
 * 方法参数检验不合法异常
 *
 * @param bindingResult BindingResult
 * @param parameter 方法参数
 */
class MethodArgumentNotValidException(bindingResult: BindingResult, val parameter: MethodParameter) :
    BindException(bindingResult) {
}