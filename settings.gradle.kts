rootProject.name = "Kotlin-Spring"


// for SpringFramework Project
include(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Framework")
include(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Instrument")
include(":Kotlin-Spring-Framework-Project:Kotlin-Spring-Web")


// for SpringBoot subProjects
include(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot")
include(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Autoconfigure")
include(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Actuator")
include(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Actuator-Autoconfigure")
include(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Devtools")
include(":Kotlin-Spring-Boot-Project:Kotlin-Spring-Boot-Loader")

// for SpringCloud subProject
include(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Context")
include(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Common")
include(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Ribbon")
include(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Nacos:Kotlin-Spring-Cloud-Nacos-Config")
include(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-Nacos:Kotlin-Spring-Cloud-Nacos-Discovery")
include(":Kotlin-Spring-Cloud-Project:Kotlin-Spring-Cloud-OpenFeign")

// for Spring Others
include(":Kotlin-Spring-Others:Kotlin-Spring-MyBatis")
include(":Kotlin-Spring-Others:Kotlin-Spring-SpringShell")
include(":Kotlin-Spring-Others:Kotlin-Nacos:Kotlin-Nacos-Config-Server")
include(":Kotlin-Spring-Others:Kotlin-Nacos:Kotlin-Nacos-Naming-Server")
include(":Kotlin-Spring-Others:Kotlin-Nacos:Kotlin-Nacos-Api")










// for Logger
include(":Kotlin-Logger:logger-api")
include(":Kotlin-Logger:logger-impl")
include(":Kotlin-Logger:logger-temp")
include(":Kotlin-Logger:logger-test")
include(":Kotlin-Logger:logger-slf4j-impl")
