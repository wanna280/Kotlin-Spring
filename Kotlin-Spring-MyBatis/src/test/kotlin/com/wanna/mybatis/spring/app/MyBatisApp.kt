package com.wanna.mybatis.spring.app

import com.alibaba.druid.pool.DruidDataSource
import com.wanna.boot.autoconfigure.SpringBootApplication
import com.wanna.boot.runSpringApplication
import com.wanna.framework.context.annotation.Bean
import com.wanna.mybatis.spring.app.mapper.MyMapper
import javax.sql.DataSource


@SpringBootApplication
class MyBatisApp {
    @Bean
    fun dataSource(): DataSource {
        val dataSource = DruidDataSource()
        dataSource.url = "jdbc:mysql://127.0.0.1/online_print"
        dataSource.username = "root"
        return dataSource
    }
}

fun main() {
    val applicationContext = runSpringApplication<MyBatisApp>()
    val myMapper = applicationContext.getBean(MyMapper::class.java)
    println(myMapper.select())
}