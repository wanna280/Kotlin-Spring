package com.wanna.mybatis.spring

import com.wanna.framework.dao.UncategorizedDataAccessException

/**
 * MyBatis的系统异常，是对Spring的[UncategorizedDataAccessException]更加具体的实现，
 * MyBatis的系统异常并不能匹配原生的Spring的Dao包当中提供的任何异常，因此我们采用的继承无法去进行分类的数据访问异常。
 *
 * 在MyBatis3当中本来就已经使用了[org.apache.ibatis.exceptions.PersistenceException]，它本身已经是一个运行时异常了；
 * 但是我们使用这个类去包装[org.apache.ibatis.exceptions.PersistenceException]，更加方便用户去进行统一处理，
 * 用户无需去捕捉更加底层的MyBatis，使用Spring提供的[com.wanna.framework.dao.DataAccessException]，可以更加统一地处理
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/15
 * @see com.wanna.framework.dao.DataAccessException
 */
class MyBatisSystemException(cause: Throwable) : UncategorizedDataAccessException(null, cause) {
    override val cause: Throwable
        get() = super.cause ?: throw AssertionError("Should not happened!")
}