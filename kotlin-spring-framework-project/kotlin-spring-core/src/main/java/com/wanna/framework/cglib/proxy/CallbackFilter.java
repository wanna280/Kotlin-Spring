package com.wanna.framework.cglib.proxy;

import java.lang.reflect.Method;

/**
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/7
 */
public interface CallbackFilter {
    int accept(Method var1);

    boolean equals(Object var1);
}