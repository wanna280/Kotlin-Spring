package com.wanna.mybatis.spring.app

import com.alibaba.druid.pool.DruidDataSource
import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.jdbc.datasource.DataSourceTransactionManager
import com.wanna.framework.transaction.PlatformTransactionManager
import com.wanna.framework.transaction.annotation.EnableTransactionManagement
import com.wanna.framework.transaction.annotation.Propagation
import com.wanna.framework.transaction.annotation.Transactional
import com.wanna.mybatis.spring.SqlSessionFactoryBean
import com.wanna.mybatis.spring.app.mapper.MyMapper
import com.wanna.mybatis.spring.mapper.MapperFactoryBean
import javax.sql.DataSource

@EnableTransactionManagement
@SpringBootApplication
open class MyBatisApp {
    @Bean
    open fun dataSource(): DataSource {
        val dataSource = DruidDataSource()
        dataSource.url = "jdbc:mysql://127.0.0.1/online_print"
        dataSource.username = "root"
        return dataSource
    }

    @Bean
    open fun platformTransactionManager(dataSource: DataSource): PlatformTransactionManager {
        val dataSourceTransactionManager = DataSourceTransactionManager()
        dataSourceTransactionManager.setDataSource(dataSource)
        return dataSourceTransactionManager
    }

}

@Component
open class Tx {

    @Autowired
    private lateinit var mapper: MyMapper

    @Autowired
    private lateinit var fb: List<MapperFactoryBean>

    @Autowired
    private lateinit var txObject: Tx

    @Transactional
    open fun tx() {
        txObject.tx2()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    open fun tx2() {
        mapper.select();
    }
}

fun main() {
    val applicationContext = runSpringApplication<MyBatisApp>()
    val tx = applicationContext.getBean(Tx::class.java)
    tx.tx()
}