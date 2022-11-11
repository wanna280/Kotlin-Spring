package com.wanna.framework.dao

/**
 * 在数据操作完成之后，啥都OK，但是我们无法完成清理工作；
 *
 * 例如当我们已经使用完成一条连接，但是我们无法关闭这条连接！
 *
 * 数据访问通常会在finally代码块当中资源的释放，因此在清理(cleanup)失败时，
 * 采用打印日志的方式去进行处理，而不是采用rethrow重新丢出去的方式去进行处理
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
open class CleanupFailureDataAccessException(message: String?, cause: Throwable?) :
    NonTransientDataAccessException(message, cause)