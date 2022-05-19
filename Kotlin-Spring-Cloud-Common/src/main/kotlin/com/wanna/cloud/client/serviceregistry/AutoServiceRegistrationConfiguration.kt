package com.wanna.cloud.client.serviceregistry

import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.framework.context.annotation.Configuration

@EnableConfigurationProperties([AutoServiceRegistrationProperties::class])
@Configuration
class AutoServiceRegistrationConfiguration {
}