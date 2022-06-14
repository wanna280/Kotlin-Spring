package com.wanna.mybatis.spring

import com.wanna.framework.transaction.support.TransactionSynchronizationManager
import org.apache.ibatis.session.ExecutorType
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory

/**
 * SqlSession的工具类
 */
object SqlSessionUtils {

    /**
     * 在有事务的情况下，可以从Spring的事务同步管理器当中获取SqlSession；
     * 如果事务同步管理器当中没有SqlSession的话，那么就得通过SqlSessionFactory去openSession去获取到SqlSession
     *
     * @param sqlSessionFactory sqlSessionFactory
     * @param executorType 执行器类型
     */
    @JvmStatic
    fun getSqlSession(sqlSessionFactory: SqlSessionFactory, executorType: ExecutorType): SqlSession {
        val sqlSessionHolder = TransactionSynchronizationManager.getResource(sqlSessionFactory) as SqlSessionHolder?
        var sqlSession = sqlSessionHolder?.sqlSession
        if (sqlSession != null) {
            return sqlSession
        }
        sqlSession = sqlSessionFactory.openSession(executorType)

        // 把SqlSession入到ThreadLocal当中
        registerSessionHolder(sqlSessionFactory, executorType, sqlSession)

        return sqlSession
    }

    /**
     * 如果必要的话，关闭SqlSession
     *
     * * 1.如果是事务的SqlSession，调用release去释放连接
     * * 2.如果不是事务的SqlSession，那么直接关闭SqlSession
     *
     * @param sqlSession SqlSession
     * @param sqlSessionFactory SqlSessionFactory
     */
    @JvmStatic
    fun closeSqlSession(sqlSession: SqlSession, sqlSessionFactory: SqlSessionFactory) {
        val sqlSessionHolder = TransactionSynchronizationManager.getResource(sqlSessionFactory) as SqlSessionHolder?
        // 如果是事务的SqlSession，调用release去释放连接
        if (sqlSessionHolder != null && sqlSessionHolder.sqlSession == sqlSession) {
            sqlSessionHolder.released()
            // 如果不是事务的SqlSession，那么直接关闭SqlSession
        } else {
            sqlSession.close()
        }
    }

    @JvmStatic
    private fun registerSessionHolder(
        sqlSessionFactory: SqlSessionFactory, executorType: ExecutorType, sqlSession: SqlSession
    ) {
        val sqlSessionHolder = SqlSessionHolder(sqlSession, executorType)
        TransactionSynchronizationManager.bindResource(sqlSessionFactory, sqlSessionHolder)
    }
}