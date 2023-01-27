package com.wanna.boot.devtools.restart

/**
 * 失败的处理器
 */
interface FailureHandler {
    companion object {
        /**
         *  None FailureHandler
         */
        @JvmStatic
        val NONE = object : FailureHandler {
            override fun handle(error: Throwable): Outcome = Outcome.ABORT
        }
    }


    /**
     * 处理产生的异常
     *
     * @param error 异常
     * @return 处理异常的结果(结束/重试?)
     */
    fun handle(error: Throwable): Outcome

    /**
     * 处理失败的结果, 是要结束? 还是要去进行尝试?
     */
    enum class Outcome { ABORT, RETRY }
}