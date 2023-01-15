package com.wanna.framework.dao

/**
 * 未分类的[DataAccessException]的实现;
 * 对于它的子类来说, 我们无法辨别更仔细的异常了, 我们无法知道是否是底层资源出现了问题？
 * 例如对于JDBC的[java.sql.SQLException]我们就无法更加精确的指出来出现异常的原因
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 * @see DataAccessException
 */
abstract class UncategorizedDataAccessException(message: String?, cause: Throwable) :
    NonTransientDataAccessException(message)