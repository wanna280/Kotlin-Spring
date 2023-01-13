package com.wanna.framework.beans

import com.wanna.framework.lang.Nullable

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/13
 */
class FatalBeanException(@Nullable message: String?, @Nullable cause: Throwable?) : BeansException(message, cause) {
    constructor(message: String) : this(message, null)
}