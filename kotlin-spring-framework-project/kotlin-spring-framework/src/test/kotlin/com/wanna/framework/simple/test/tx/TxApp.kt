package com.wanna.framework.simple.test.tx

import com.alibaba.druid.pool.DruidDataSource
import com.wanna.framework.context.annotation.AnnotationConfigApplicationContext
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.jdbc.datasource.ConnectionHolder
import com.wanna.framework.jdbc.datasource.DataSourceTransactionManager
import com.wanna.framework.transaction.PlatformTransactionManager
import com.wanna.framework.transaction.annotation.EnableTransactionManagement
import com.wanna.framework.transaction.annotation.Transactional
import com.wanna.framework.transaction.support.TransactionSynchronizationManager
import javax.sql.DataSource

@EnableTransactionManagement
@Configuration(proxyBeanMethods = false)
open class TxApp {

    class MyException : Exception()

    @Autowired
    private lateinit var txApp: TxApp

    private lateinit var dataSource: DataSource

    @Bean
    open fun txManager(dataSource: DataSource): PlatformTransactionManager {
        val transactionManager = DataSourceTransactionManager()
        transactionManager.setDataSource(dataSource)
        this.dataSource = dataSource
        return transactionManager
    }

    @Bean
    open fun dataSource(): DataSource {
        val dataSource = DruidDataSource()
        dataSource.url = "jdbc:mysql://127.0.0.1/online_print"
        dataSource.username = "root"
        return dataSource
    }

    @Transactional(rollbackFor = [MyException::class])
    open fun tx1() {
        val connectionHolder = TransactionSynchronizationManager.getResource(dataSource) as ConnectionHolder?
        val connection = connectionHolder!!.connection
        val statement = connection.createStatement()
        val update = statement.executeUpdate("insert into test (name, age) values ('wanna', 18)")
        throw MyException()
    }

    @Transactional
    open fun tx2() {
        val connectionHolder = TransactionSynchronizationManager.getResource(dataSource) as ConnectionHolder?
        println(connectionHolder)
        tx3()
    }

    @Transactional
    open fun tx3() {
        val connectionHolder = TransactionSynchronizationManager.getResource(dataSource) as ConnectionHolder?
        println(connectionHolder)
    }

}

fun main() {
    val applicationContext = AnnotationConfigApplicationContext(TxApp::class.java)
    val txApp = applicationContext.getBean(TxApp::class.java)
    txApp.tx1()
}