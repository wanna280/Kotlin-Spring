package com.wanna.mybatis.spring.support

import com.wanna.framework.beans.factory.InitializingBean
import com.wanna.mybatis.spring.SqlSessionTemplate
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory

/**
 * 提供了SqlSession的Dao访问的支持，组合了SqlSessionTemplate，支持去进行SqlSession的获取
 *
 * @see com.wanna.mybatis.spring.mapper.MapperFactoryBean
 */
open class SqlSessionDaoSupport : InitializingBean {

    // SqlSessionTemplate
    private var sqlSessionTemplate: SqlSessionTemplate? = null

    override fun afterPropertiesSet() {
        checkDaoConfig()
    }

    /**
     * 设置SqlSessionFactory，如果此时SqlSessionTemplate为空，还需要去创建SqlSessionFactory；
     *
     * @param sqlSessionFactory SqlSessionFactory
     */
    open fun setSqlSessionFactory(sqlSessionFactory: SqlSessionFactory) {
        if (this.sqlSessionTemplate == null || sqlSessionFactory != sqlSessionTemplate!!.sqlSessionFactory) {
            this.sqlSessionTemplate = createSqlSessionTemplate(sqlSessionFactory)
        }
    }

    /**
     * 给定SqlSessionFactory，去创建SqlSessionTemplate
     *
     * @return SqlSessionTemplate
     */
    protected open fun createSqlSessionTemplate(sqlSessionFactory: SqlSessionFactory): SqlSessionTemplate {
        return SqlSessionTemplate(sqlSessionFactory)
    }

    protected open fun checkDaoConfig() {
        checkNotNull(sqlSessionTemplate) { "sqlSessionTemplate不能为空" }
    }

    open fun setSqlSessionTemplate(sqlSessionTemplate: SqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate
    }

    open fun getSqlSessionTemplate(): SqlSessionTemplate =
        this.sqlSessionTemplate ?: throw IllegalStateException("SqlSessionTemplate不能为空")

    open fun getSqlSession(): SqlSession = this.sqlSessionTemplate ?: throw IllegalStateException("SqlSession不能为空")
}