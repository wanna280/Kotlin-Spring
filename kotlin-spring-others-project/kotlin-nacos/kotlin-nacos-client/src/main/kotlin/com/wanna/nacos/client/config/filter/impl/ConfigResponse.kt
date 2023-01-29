package com.wanna.nacos.client.config.filter.impl

import com.wanna.nacos.client.config.filter.impl.ConfigContext
import com.wanna.nacos.api.config.filter.IConfigContext
import com.wanna.nacos.api.config.filter.IConfigResponse
import com.wanna.nacos.client.config.common.ConfigConstants.CONFIG_TYPE
import com.wanna.nacos.client.config.common.ConfigConstants.CONTENT
import com.wanna.nacos.client.config.common.ConfigConstants.DATA_ID
import com.wanna.nacos.client.config.common.ConfigConstants.ENCRYPTED_DATA_KEY
import com.wanna.nacos.client.config.common.ConfigConstants.GROUP
import com.wanna.nacos.client.config.common.ConfigConstants.TENANT

/**
 * Config Response
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
class ConfigResponse : IConfigResponse {

    /**
     * Params
     */
    private val params = LinkedHashMap<String, Any?>()

    /**
     * ConfigContext
     */
    private val configContext: IConfigContext = ConfigContext()

    override fun getParameter(name: String): Any? {
        return this.params[name]
    }

    override fun putParameter(name: String, value: Any?) {
        this.params[name] = value
    }

    override fun getConfigContext(): IConfigContext {
        return this.configContext
    }

    fun setContent(content: String?) {
        params[CONTENT] = content
    }

    fun getContent(): String? {
        return params[CONTENT] as String?
    }

    fun setDataId(dataId: String?) {
        params[DATA_ID] = dataId
    }

    fun getDataId(): String? {
        return params[DATA_ID] as String?
    }

    fun setTenant(tenant: String?) {
        params[TENANT] = tenant
    }

    fun getTenant(): String? {
        return params[TENANT] as String?
    }

    fun setGroup(group: String?) {
        params[GROUP] = group
    }

    fun getGroup(): String? {
        return params[GROUP] as String?
    }

    fun setConfigType(configType: String?) {
        params[CONFIG_TYPE] = configType
    }

    fun getConfigType(): String? {
        return params[CONFIG_TYPE] as String?
    }

    fun setEncryptedDataKey(encryptedDataKey: String?) {
        params[ENCRYPTED_DATA_KEY] = encryptedDataKey
    }

    fun getEncryptedDataKey(): String? {
        return params[ENCRYPTED_DATA_KEY] as String?
    }
}