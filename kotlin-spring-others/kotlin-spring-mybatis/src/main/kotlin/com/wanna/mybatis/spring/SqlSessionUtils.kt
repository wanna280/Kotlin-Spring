package com.wanna.mybatis.spring

import com.wanna.framework.dao.support.PersistenceExceptionTranslator
import com.wanna.framework.transaction.support.TransactionSynchronizationManager
import org.apache.ibatis.session.ExecutorType
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory
import org.slf4j.LoggerFactory

/**
 * SqlSession的单例工具类, 提供从Spring的TransactionManager当中去进行SqlSession的获取、注册、关闭等操作
 *
 * @see TransactionSynchronizationManager
 */
object SqlSessionUtils {

    /**
     * Logger
     */
    private val logger = LoggerFactory.getLogger(SqlSessionUtils::class.java)

    /**
     * 在有事务的情况下, 可以从Spring的事务同步管理器当中获取SqlSession;
     * 如果事务同步管理器当中没有SqlSession的话, 那么就得通过SqlSessionFactory去openSession去获取到SqlSession
     *
     * @param sqlSessionFactory sqlSessionFactory
     * @param executorType 执行器类型
     */
    @JvmStatic
    fun getSqlSession(
        sqlSessionFactory: SqlSessionFactory,
        executorType: ExecutorType,
        exceptionTranslator: PersistenceExceptionTranslator
    ): SqlSession {

        // 1.如果存在有事务的话, 那么可以从事务同步管理器当中去获取SqlSession, 这样就保证每次CRUD操作, 都获取到的是该SqlSession
        val sqlSessionHolder = TransactionSynchronizationManager.getResource(sqlSessionFactory) as SqlSessionHolder?
        var sqlSession = sqlSessionHolder?.sqlSession
        if (sqlSession != null) {
            return sqlSession
        }

        // 如果不存在已经有的事务的话, 那么使用SqlSessionFactory.openSession去获取到SqlSession
        sqlSession = sqlSessionFactory.openSession(executorType)

        if (logger.isDebugEnabled) {
            logger.debug("正在创建一个新的SqlSession")
        }

        // 把SqlSession入到事务同步管理器的ThreadLocal当中, 下次再去getSqlSession, 就可以从ThreadLocal当中去进行获取了
        registerSessionHolder(sqlSessionFactory, executorType, exceptionTranslator, sqlSession)

        return sqlSession
    }

    /**
     * 如果必要的话, 需要去关闭SqlSession
     *
     * * 1.如果是事务的SqlSession, 调用release去释放连接
     * * 2.如果不是事务的SqlSession, 那么直接关闭SqlSession
     *
     * @param sqlSession SqlSession
     * @param sqlSessionFactory SqlSessionFactory
     */
    @JvmStatic
    fun closeSqlSession(sqlSession: SqlSession, sqlSessionFactory: SqlSessionFactory) {
        val sqlSessionHolder = TransactionSynchronizationManager.getResource(sqlSessionFactory) as SqlSessionHolder?
        // 如果是事务的SqlSession, 调用release去释放连接
        if (sqlSessionHolder != null && sqlSessionHolder.sqlSession == sqlSession) {
            sqlSessionHolder.released()
            // 如果不是事务的SqlSession, 那么直接关闭SqlSession
        } else {
            sqlSession.close()
        }
    }

    /**
     * 注册SqlSessionHolder到事务同步管理器当中
     *
     * @param sqlSessionFactory SqlSessionFactory
     * @param executorType 执行器类型(SIMPLE/BATCH/REUSE)
     * @param exceptionTranslator 持久层异常的翻译器(保存到SqlSessionHolder当中)
     * @param sqlSession SqlSession(保存到SqlSessionHolder当中)
     */
    @JvmStatic
    private fun registerSessionHolder(
        sqlSessionFactory: SqlSessionFactory,
        executorType: ExecutorType,
        exceptionTranslator: PersistenceExceptionTranslator,
        sqlSession: SqlSession
    ) {
        val sqlSessionHolder = SqlSessionHolder(sqlSession, executorType, exceptionTranslator)
        TransactionSynchronizationManager.bindResource(sqlSessionFactory, sqlSessionHolder)
    }
}