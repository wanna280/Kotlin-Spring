package com.wanna.framework.jdbc.datasource

import java.sql.Connection
import javax.sql.DataSource

interface SmartDataSource : DataSource {
    fun shouldClose(connection: Connection) : Boolean
}