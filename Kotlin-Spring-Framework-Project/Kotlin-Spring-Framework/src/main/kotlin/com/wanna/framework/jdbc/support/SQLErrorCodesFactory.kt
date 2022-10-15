package com.wanna.framework.jdbc.support

import javax.sql.DataSource

/**
 * SQLErrorCodes的单例工厂
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/16
 */
object SQLErrorCodesFactory {
    fun getErrorCodes(dataSource: DataSource): SQLErrorCodes {
        // TODO
        return SQLErrorCodes()
    }
}