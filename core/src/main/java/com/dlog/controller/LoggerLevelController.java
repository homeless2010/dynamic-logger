package com.dlog.controller;

import com.step.dlog.bean.LoggerBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.logging.*;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequestMapping("/logger/level")
@RequiredArgsConstructor
public class LoggerLevelController implements BaseController{
    private static final Map<String, LogLevel> LEVELS = new HashMap<>();

    static {
        LEVELS.put("trace", LogLevel.TRACE);
        LEVELS.put("debug", LogLevel.DEBUG);
        LEVELS.put("info", LogLevel.INFO);
        LEVELS.put("warn", LogLevel.WARN);
        LEVELS.put("error", LogLevel.ERROR);
        LEVELS.put("off", LogLevel.OFF);
    }

    private final ApplicationContext context;

    /**
     * 设置日志级别
     */
    @PostMapping
    public List<String> changeLoggerLevel(@RequestBody LoggerBean loggerBean) {
        String name = loggerBean.getName();
        LogLevel logLevel = LEVELS.getOrDefault(loggerBean.getLevel(), LogLevel.INFO);
        LoggingSystem loggingSystem = context.getBean(LoggingApplicationListener.LOGGING_SYSTEM_BEAN_NAME, LoggingSystem.class);
        //按分组设置日志级别
        LoggerGroups loggerGroups = context.getBean(LoggingApplicationListener.LOGGER_GROUPS_BEAN_NAME, LoggerGroups.class);
        LoggerGroup group = loggerGroups.get(name);
        if (group != null && group.hasMembers()) {
            group.configureLogLevel(logLevel, loggingSystem::setLogLevel);
            List<String> loggerConfigs = group.getMembers().stream()
                    .map(loggingSystem::getLoggerConfiguration)
                    .sorted(Comparator.comparing(LoggerConfiguration::getName))
                    .map(LoggerConfiguration::toString)
                    .collect(Collectors.toList());
            return loggerConfigs;
        }
        //只针对单个loggerName设置日志级别
        LoggerConfiguration loggerConfiguration = loggingSystem.getLoggerConfiguration(name);
        //如果loggerName对应的Logger不存在不应该修改其日志级别,如果日志使用的是logback,可能导致内存泄漏
        //参见 ch.qos.logback.classic.LoggerContext.getLogger(java.lang.String) L142
        //在SpringBoot2.0.9中这个判断不生效,即使loggerName不存在同样能够获取的LoggerConfiguration,注意测试
        if (loggerConfiguration == null) {
            return Collections.singletonList("不存在loggerName=" + name);
        }
        loggingSystem.setLogLevel(name, logLevel);
        return Collections.singletonList(loggingSystem.getLoggerConfiguration(name).toString());
    }

    @GetMapping("confs")
    public List<LoggerConfiguration> getLoggerConfigs(String name) {
        LoggingSystem loggingSystem = context.getBean(LoggingApplicationListener.LOGGING_SYSTEM_BEAN_NAME, LoggingSystem.class);
        Stream<LoggerConfiguration> configurationStream = loggingSystem
                .getLoggerConfigurations()
                .stream();
        if (StringUtils.isNotBlank(name)) {
            return configurationStream
                    .filter(c -> StringUtils.contains(c.getName(), name))
                    .sorted(Comparator.comparing(LoggerConfiguration::getName))
                    .collect(Collectors.toList());
        }
        return configurationStream
                .sorted(Comparator.comparing(LoggerConfiguration::getName))
                .collect(Collectors.toList());
    }

    @RequestMapping("/test")
    public void test() {
        log.debug("debug");
        log.info("info");
        log.warn("warn");
        log.error("error");
    }
}
