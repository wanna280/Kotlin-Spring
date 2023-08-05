package com.wanna.cloud.context.scope.thread

import com.wanna.cloud.context.scope.GenericScope

/**
 * 线程的作用域, ThreadScope内的Bean, 是线程内独有的
 */
open class ThreadScope : GenericScope() {
    init {
        this.setName("thread")
        this.setCache(ThreadLocalScopeCache())
    }
}