rootProject.name = "kotlin-spring"


// for springframework project
include(":kotlin-spring-framework-project:kotlin-spring-framework")
include(":kotlin-spring-framework-project:kotlin-spring-instrument")
include(":kotlin-spring-framework-project:kotlin-spring-web")
include(":kotlin-spring-framework-project:kotlin-spring-test")


// for spring boot subprojects
include(":kotlin-spring-boot-project:kotlin-spring-boot")
include(":kotlin-spring-boot-project:kotlin-spring-boot-autoconfigure")
include(":kotlin-spring-boot-project:kotlin-spring-boot-actuator")
include(":kotlin-spring-boot-project:kotlin-spring-boot-actuator-autoconfigure")
include(":kotlin-spring-boot-project:kotlin-spring-boot-devtools")
include(":kotlin-spring-boot-project:kotlin-spring-boot-loader")
include(":kotlin-spring-boot-project:kotlin-spring-boot-test")



// for spring cloud subproject
include(":kotlin-spring-cloud-project:kotlin-spring-cloud-context")
include(":kotlin-spring-cloud-project:kotlin-spring-cloud-common")
include(":kotlin-spring-cloud-project:kotlin-spring-cloud-ribbon")
include(":kotlin-spring-cloud-project:kotlin-spring-cloud-nacos:kotlin-spring-cloud-nacos-config")
include(":kotlin-spring-cloud-project:kotlin-spring-cloud-nacos:kotlin-spring-cloud-nacos-discovery")
include(":kotlin-spring-cloud-project:kotlin-spring-cloud-openfeign")



// for spring others
include(":kotlin-spring-others:kotlin-spring-mybatis")
include(":kotlin-spring-others:kotlin-spring-shell")
include(":kotlin-spring-others:kotlin-nacos:kotlin-nacos-config-server")
include(":kotlin-spring-others:kotlin-nacos:kotlin-nacos-naming-server")
include(":kotlin-spring-others:kotlin-nacos:kotlin-nacos-api")
include(":kotlin-spring-others:kotlin-nacos:kotlin-nacos-client")


// for logger impl
include(":kotlin-logger:logger-api")
include(":kotlin-logger:logger-impl")
include(":kotlin-logger:logger-temp")
include(":kotlin-logger:logger-test")
include(":kotlin-logger:logger-slf4j-impl")
