package com.wanna.boot.logging

import com.wanna.common.logging.Logger
import java.util.function.Supplier

/**
 * [DeferredLogFactory]的实现, 提供获取到[DeferredLog]的相关工厂方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
open class DeferredLogs : DeferredLogFactory {

    /**
     * 存放所有的[DeferredLog]当中, 待进行输出的日志行列表
     */
    private val lines = DeferredLog.Lines()

    /**
     * 存放当前[DeferredLogs]当中的所有的[DeferredLog]列表
     */
    private val loggers = ArrayList<DeferredLog>()

    /**
     * 将所有的[DeferredLog]去切换成为正常的非延时输出的[Logger]
     *
     * @see DeferredLog.switchOver
     */
    fun switchOverAll() {
        synchronized(this.lines) {
            // 切换之前, 肯定需要先把所有待输出的日志行, 分别去执行输出...
            for (line in this.lines) {
                DeferredLog.logTo(line.destination, line.level, line.message, line.args, line.throwable)
            }

            // 将所有的Logger的模式都切换成为正常模式...
            for (logger in loggers) {
                logger.switchOver()
            }

            // clear掉所有的延时日志行列表
            this.lines.clear()
        }
    }

    /**
     * 获取到支持去进行日志的延时输出的[DeferredLog]
     *
     * @param destination 懒加载获取到[Logger]的回调函数
     * @return 支持去进行日志的延时输出的[Logger]
     */
    override fun getLog(destination: Supplier<Logger>): Logger {
        synchronized(this.loggers) {
            val logger = DeferredLog(destination, lines)
            loggers += logger
            return logger
        }
    }
}