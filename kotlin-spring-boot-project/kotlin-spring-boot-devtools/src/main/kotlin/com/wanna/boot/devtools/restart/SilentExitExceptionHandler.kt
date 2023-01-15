package com.wanna.boot.devtools.restart

import java.lang.reflect.InvocationTargetException

/**
 * 把当前线程去进行默默退出的ExceptionHandler, 我们有需要直接退出当前线程的需求,
 * 但是默认情况下线程的退出时, 会存在有ExceptionHandler去直接把异常的调用栈打出来,
 * 但是实际上呢, 我们并不需要它把异常栈打出来, 我们要做的只是直接退出当前线程就行了
 *
 * @param delegate 当异常类型不是SilentExitException时, 应该怎么去处理？
 * 我们就需要组合一个delegate去完成(一般为线程的原始ExceptionHandler)
 */
class SilentExitExceptionHandler(private val delegate: Thread.UncaughtExceptionHandler) :
    Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        // 如果异常的类型的SilentExitException, 那么直接return(退出当前线程), 别把异常打出来了
        if (e is SilentExitException || (e is InvocationTargetException && e.targetException is SilentExitException)) {
            return
        }
        // 如果不是SilentExitExceptionHandler, 那么直接使用delegate去完成未捕捉的异常处理
        delegate.uncaughtException(t, e)
    }

    /**
     * 阻止非0的ExitCode产生
     */
    private fun preventNonZeroExitCode() {
        System.exit(0)
    }

    companion object {

        /**
         * 将SilentExceptionHandler绑定给当前线程的UncaughtExceptionHandler当中
         *
         * @param thread 要去进行绑定的线程
         */
        @JvmStatic
        fun setup(thread: Thread) {
            val uncaughtExceptionHandler = thread.uncaughtExceptionHandler
            if (uncaughtExceptionHandler !is SilentExitExceptionHandler) {
                thread.uncaughtExceptionHandler = SilentExitExceptionHandler(uncaughtExceptionHandler)
            }
        }

        /**
         * 退出当前线程, 直接丢出SilentExitException, 并直接交给SilentExitExceptionHandler去退出当前线程即可
         *
         * @see SilentExitException
         */
        @JvmStatic
        fun exitCurrentThread(): Nothing = throw SilentExitException()
    }

    /**
     * 默认默默退出的异常, 起标识作用, 标识抛出这个异常的线程需要去直接退出
     *
     * @see SilentExitExceptionHandler
     */
    private class SilentExitException : RuntimeException()
}