package com.wanna.framework.web.bind.annotation

/**
 * 标识该参数, 需要从HTTP请求的RequestBody当中去进行获取和转换, 可以支持使用Map/JavaBean去进行接收
 *
 * 具体的使用参考下面的示例代码:
 *
 * ```kotlin
 * @RequestMapping("/handle")
 * fun handle(@RequestBody body: Map<String, String>) : Map<String, String> {
 *     // do something
 * }
 * ```
 *
 * @param required RequestBody是否为必要的? 如果为必要的, 但是没有给出, 会抛出异常
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE_PARAMETER)
annotation class RequestBody(val required: Boolean = true)
