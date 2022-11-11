# 1. BeanDefinition的分类

* 1.通过AnnotatedBeanDefinitionReader进行注解Bean注册得到的BeanDefinition为AnnotatedGenericBeanDefinition
* 2.注册默认的注解的组件时，全部都是使用的是RootBeanDefinition
* 3.对于ClassPathBeanDefinitionScanner完成ComponentScan，得到的是ScannedGenericBeanDefinition

所有的BeanDefinition在最终都会使用merge成为一个RootBeanDefinition

# 2. 对于BeanNameGenerator的设置

* 1.如果在ConfigurationClassPostProcessor中进行配置了setBeanNameGenerator，那么就采用这个作为BeanNameGenerator
* 2.如果你没有配置BeanDefinitionGenerator，那么从SingleBeanRegistry中尝试去进行获取BeanDefinitionGenerator并进行使用。(通过AnnotationConfigApplicationContext的setBeanNameGenerator，会往容器中注册BeanNameGenerator)
* 3.如果SingleBeanRegistry中没有获取到，那么就使用默认的(普通的Bean使用AnnotationBeanNameGenerator生成，Import的Bean采用FullyQualifiedAnnotationBeanNameGenerator生成)

# 3. AutowireCandidateResolver

* 1.父接口--AutowireCandidateResolver
* 2.简单实现--SimpleAutowireCandidateResolver
* 3.支持泛型--GenericTypeAwareAutowireCandidateResolver
* 4.支持Qualifier相关注解--QualifierAnnotationAutowireCandidateResolver
* 5.支持Context相关注解(例如Lazy)--ContextAnnotationAutowireCandidateResolver

# 4. BeanDefinition的各种Scanner/Reader/Parser

* 1.ConfigurationClassPostProcessor
  * ConfigurationClassParser完成配置类的解析，将每个配置类解析成为一个ConfigurationClass
    * ComponentScanAnnotationParser，完成ComponentScan注解的解析，并注册到容器当中
      * ClassPathBeanDefinitionScanner-->doScan扫描包，可以注册注解通用处理器
  * ConfigurationClassBeanDefinitionReader，对ConfigurationClassParser解析出来的ConfigurationClass中的Import组件，以及@Bean方法去进行解析和注册

* 2.AnnotationConfigApplicationContext
  * AnnotatedBeanDefinitionReader，处理配置类的各种注解(registerBean)，比如Lazy/Primary/Role/Description，并注册到容器当中，可以注册注解通用处理器
  * ClassPathBeanDefinitionScanner->doScan扫描包，可以注册注解通用处理器，扫描出来ScannedGenericBeanDefinition。

