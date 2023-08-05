package com.wanna.framework.web.bind.annotation

/**
 * 值的常量信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
object ValueConstants {
    /**
     * 默认为空的常量字符串, 因为注解不能配置为null, 因此需要用上空的常量;
     * 但是不能设置为"", 因为很多时候""是合法的, 我们不能把合法值去作为空值
     *
     * @see RequestParam.defaultValue
     * @see RequestHeader.defaultValue
     */
    const val DEFAULT_NONE = "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n"
}