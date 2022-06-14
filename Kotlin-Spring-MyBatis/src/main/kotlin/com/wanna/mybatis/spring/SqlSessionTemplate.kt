package com.wanna.mybatis.spring

import org.apache.ibatis.cursor.Cursor
import org.apache.ibatis.executor.BatchResult
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.ExecutorType
import org.apache.ibatis.session.ResultHandler
import org.apache.ibatis.session.RowBounds
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.defaults.DefaultSqlSession
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.sql.Connection

/**
 * SqlSessionTemplate，全部操作通过SqlSessionProxy代理去完成，SqlSessionProxy又交给DefaultSqlSession去执行
 *
 * @see SqlSession
 */
class SqlSessionTemplate(
    val sqlSessionFactory: SqlSessionFactory,
    val executorType: ExecutorType = ExecutorType.SIMPLE
) : SqlSession {

    // 使用Jdk动态代理，生成SqlSessionProxy，把执行目标方法的逻辑交给委托的SqlSession去进行完成
    private var sqlSessionProxy: SqlSession = Proxy.newProxyInstance(
        SqlSessionTemplate::class.java.classLoader,
        arrayOf(SqlSession::class.java),
        SqlSessionInterceptor(),
    ) as SqlSession

    private fun getSqlSessionProxy(): SqlSession = this.sqlSessionProxy

    /**
     * SqlSession的拦截器，负责将SqlSessionProxy的方法去使用DefaultSqlSession去进行委托执行
     *
     * @see SqlSession
     * @see DefaultSqlSession
     */
    inner class SqlSessionInterceptor : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method, args: Array<Any?>?): Any? {
            val sqlSession = SqlSessionUtils.getSqlSession(sqlSessionFactory, executorType)
            try {
                return if (args == null) method.invoke(sqlSession) else method.invoke(sqlSession, *args)
            } catch (ex: Throwable) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory)
                throw ex
            } finally {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory)
            }
        }
    }


    override fun <T : Any?> selectOne(statement: String?): T = getSqlSessionProxy().selectOne(statement)

    override fun <T : Any?> selectOne(statement: String?, parameter: Any?): T =
        getSqlSessionProxy().selectOne(statement, parameter)

    override fun <E : Any?> selectList(statement: String?): MutableList<E> = getSqlSessionProxy().selectList(statement)

    override fun <E : Any?> selectList(statement: String?, parameter: Any?): MutableList<E> =
        getSqlSessionProxy().selectList(statement, parameter)

    override fun <E : Any?> selectList(statement: String?, parameter: Any?, rowBounds: RowBounds?): MutableList<E> =
        getSqlSessionProxy().selectList(statement, parameter, rowBounds)

    override fun <K : Any?, V : Any?> selectMap(statement: String?, mapKey: String?): MutableMap<K, V> =
        getSqlSessionProxy().selectMap(statement, mapKey)

    override fun <K : Any?, V : Any?> selectMap(
        statement: String?, parameter: Any?, mapKey: String?
    ): MutableMap<K, V> = getSqlSessionProxy().selectMap(statement, parameter, mapKey)

    override fun <K : Any?, V : Any?> selectMap(
        statement: String?, parameter: Any?, mapKey: String?, rowBounds: RowBounds?
    ): MutableMap<K, V> = getSqlSessionProxy().selectMap(statement, parameter, mapKey, rowBounds)

    override fun <T : Any?> selectCursor(statement: String?): Cursor<T> = getSqlSessionProxy().selectCursor(statement)

    override fun <T : Any?> selectCursor(statement: String?, parameter: Any?): Cursor<T> =
        getSqlSessionProxy().selectCursor(statement, parameter)

    override fun <T : Any?> selectCursor(statement: String?, parameter: Any?, rowBounds: RowBounds?): Cursor<T> =
        getSqlSessionProxy().selectCursor(statement, parameter, rowBounds)

    override fun select(statement: String?, parameter: Any?, handler: ResultHandler<*>?) =
        getSqlSessionProxy().select(statement, parameter, handler)

    override fun select(statement: String?, handler: ResultHandler<*>?) =
        getSqlSessionProxy().select(statement, handler)

    override fun select(statement: String?, parameter: Any?, rowBounds: RowBounds?, handler: ResultHandler<*>?) =
        getSqlSessionProxy().select(statement, parameter, rowBounds, handler)

    override fun insert(statement: String?): Int = getSqlSessionProxy().insert(statement)

    override fun insert(statement: String?, parameter: Any?): Int = getSqlSessionProxy().insert(statement, parameter)

    override fun update(statement: String?): Int = getSqlSessionProxy().update(statement)

    override fun update(statement: String?, parameter: Any?): Int = getSqlSessionProxy().update(statement, parameter)

    override fun delete(statement: String?): Int = getSqlSessionProxy().delete(statement)

    override fun delete(statement: String?, parameter: Any?): Int = getSqlSessionProxy().delete(statement, parameter)

    override fun commit() = throw UnsupportedOperationException("不支持这样的操作")

    override fun commit(force: Boolean) = throw UnsupportedOperationException("不支持这样的操作")

    override fun rollback() = throw UnsupportedOperationException("不支持这样的操作")

    override fun rollback(force: Boolean) = throw UnsupportedOperationException("不支持这样的操作")

    override fun close() = throw UnsupportedOperationException("不支持这样的操作")

    override fun flushStatements(): MutableList<BatchResult> = getSqlSessionProxy().flushStatements()

    override fun clearCache() = getSqlSessionProxy().clearCache()

    override fun getConfiguration(): Configuration = getSqlSessionProxy().configuration

    override fun <T : Any?> getMapper(type: Class<T>?): T = getSqlSessionProxy().getMapper(type)

    override fun getConnection(): Connection = getSqlSessionProxy().connection
}