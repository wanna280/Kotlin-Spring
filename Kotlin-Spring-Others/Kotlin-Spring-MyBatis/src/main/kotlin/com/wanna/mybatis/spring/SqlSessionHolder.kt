package com.wanna.mybatis.spring

import com.wanna.framework.transaction.support.ResourceHolderSupport
import org.apache.ibatis.session.ExecutorType
import org.apache.ibatis.session.SqlSession

class SqlSessionHolder(val sqlSession: SqlSession,val executorType: ExecutorType) : ResourceHolderSupport() {

}