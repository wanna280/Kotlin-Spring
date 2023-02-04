rootProject.name = "kotlin-spring"


// for springframework project
include(":kotlin-spring-framework-project:kotlin-spring-core")
include(":kotlin-spring-framework-project:kotlin-spring-context")
include(":kotlin-spring-framework-project:kotlin-spring-aop")
include(":kotlin-spring-framework-project:kotlin-spring-beans")
include(":kotlin-spring-framework-project:kotlin-spring-instrument")
include(":kotlin-spring-framework-project:kotlin-spring-test")
include(":kotlin-spring-framework-project:kotlin-spring-jcl")
include(":kotlin-spring-framework-project:kotlin-spring-web")


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
include(":kotlin-spring-others-project:kotlin-spring-mybatis")
include(":kotlin-spring-others-project:kotlin-spring-shell")
include(":kotlin-spring-others-project:kotlin-nacos:kotlin-nacos-config-server")
include(":kotlin-spring-others-project:kotlin-nacos:kotlin-nacos-naming-server")
include(":kotlin-spring-others-project:kotlin-nacos:kotlin-nacos-api")
include(":kotlin-spring-others-project:kotlin-nacos:kotlin-nacos-client")


// for logger impl
include(":kotlin-logger-project:kotlin-logger-api")
include(":kotlin-logger-project:kotlin-logger-impl")
include(":kotlin-logger-project:kotlin-logger-temp")
include(":kotlin-logger-project:kotlin-logger-slf4j-impl")


// for metrics project
include(":kotlin-metrics-project")
include(":kotlin-metrics-project:kotlin-metrics")


// for debugger project
include(":kotlin-debugger-project")
include(":kotlin-debugger-project:kotlin-debugger-jvm")
include(":kotlin-debugger-project:kotlin-debugger-bistoury")
include(":kotlin-debugger-project:kotlin-debugger-bistoury:kotlin-debugger-bistoury-spy")
include(":kotlin-debugger-project:kotlin-debugger-bistoury:kotlin-debugger-bistoury-agent")
include(":kotlin-debugger-project:kotlin-debugger-bistoury:kotlin-debugger-bistoury-client")