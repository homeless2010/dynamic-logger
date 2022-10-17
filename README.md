# dynamic-logger

基于springboot动态修改日志级别组件
## 原理
通过SpringBoot中对日志系统的抽象LoggingSystem来动态改变日志级别
## 配置
* spring.dynamic.logger.urlPattern          //控制台uri 默认/dlog
* spring.dynamic.logger.allow               //ip白名单 默认127.0.0.1
* spring.dynamic.logger.deny                //ip黑名单
* spring.dynamic.logger.loginUsername       //用户名 默认admin
* spring.dynamic.logger.loginPassword       //密码   默认admin123456
## 背景
实现动态改变日志级别主要有以下方式
1. 通过编码方式(setLevel)
2. 通过日志系统的自动刷新机制
    > 如logback配置```<configuration debug="false" scan="true" scanPeriod="30 seconds" packagingData="true">```
3. jmx方式
    > 如[logback jmx](https://logback.qos.ch/manual/jmxConfig.html)
4. SpringBootActuator方式
5. Arthas改变日志级别
## 参考
[美团](https://tech.meituan.com/2017/02/17/change-log-level.html)