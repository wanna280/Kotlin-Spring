package com.wanna.mybatis.spring

import com.wanna.framework.dao.support.PersistenceExceptionTranslator
import com.wanna.framework.transaction.support.ResourceHolderSupport
import org.apache.ibatis.session.ExecutorType
import org.apache.ibatis.session.SqlSession

/**
 * 包装一个SqlSession的ResourceHolder
 *
 * @param sqlSession 要去进行包装给定SqlSession
 * @param executorType MyBatis的执行器类型
 */
open class SqlSessionHolder(
    val sqlSession: SqlSession,
    val executorType: ExecutorType,
    val exceptionTranslator: PersistenceExceptionTranslator
) : ResourceHolderSupport()