package com.step.dlog.service;

import com.google.gson.Gson;
import com.step.dlog.bean.LoggerBean;
import com.step.dlog.util.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.logging.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public final class LoggerService {
    public static final int RESULT_CODE_SUCCESS = 1;
    public static final int RESULT_CODE_ERROR = -1;
    private static final LoggerService instance = new LoggerService();

    private static final Map<String, LogLevel> LEVELS = new HashMap<>();

    static {
        LEVELS.put("trace", LogLevel.TRACE);
        LEVELS.put("debug", LogLevel.DEBUG);
        LEVELS.put("info", LogLevel.INFO);
        LEVELS.put("warn", LogLevel.WARN);
        LEVELS.put("error", LogLevel.ERROR);
        LEVELS.put("off", LogLevel.OFF);
    }

    public static LoggerService getInstance() {
        return instance;
    }

    public String service(String url, Map<String, String> param) {
//        Map<String, String> parameters = getParameters(url);
        if (url.equals("/logger/level.json")) {
            LoggerBean loggerBean = new LoggerBean();
            loggerBean.setName(param.get("name"));
            loggerBean.setLevel(param.get("level"));
            return returnJSONResult(RESULT_CODE_SUCCESS, changeLoggerLevel(loggerBean));
        }
        if (url.startsWith("/logger/level/confs.json")) {
            return returnJSONResult(RESULT_CODE_SUCCESS, getLoggerConfigs(param.get("name")));
        }
        return returnJSONResult(RESULT_CODE_ERROR, "Do not support this request, please contact with administrator.");
    }

    public static String returnJSONResult(int resultCode, Object content) {
        Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
        dataMap.put("ResultCode", resultCode);
        dataMap.put("Content", content);
        Gson gson = new Gson();
        return gson.toJson(dataMap);
    }

    public static Map<String, String> getParameters(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            return Collections.<String, String>emptyMap();
        }

        String parametersStr = StringUtils.substring(url, url.indexOf("?"));
        if (parametersStr == null || parametersStr.length() == 0) {
            return Collections.<String, String>emptyMap();
        }

        String[] parametersArray = parametersStr.split("&");
        Map<String, String> parameters = new LinkedHashMap<String, String>();

        for (String parameterStr : parametersArray) {
            int index = parameterStr.indexOf("=");
            if (index <= 0) {
                continue;
            }

            String name = parameterStr.substring(0, index);
            String value = parameterStr.substring(index + 1);
            parameters.put(name, value);
        }
        return parameters;
    }

    private List<LoggerConfiguration> getLoggerConfigs(String name) {
        LoggingSystem loggingSystem = SpringUtils.getBean(LoggingApplicationListener.LOGGING_SYSTEM_BEAN_NAME, LoggingSystem.class);
        Stream<LoggerConfiguration> configurationStream = loggingSystem.getLoggerConfigurations().stream();
        if (StringUtils.isNotBlank(name)) {
            return configurationStream.filter(c -> StringUtils.contains(c.getName(), name)).sorted(Comparator.comparing(LoggerConfiguration::getName)).collect(Collectors.toList());
        }
        return configurationStream.sorted(Comparator.comparing(LoggerConfiguration::getName)).collect(Collectors.toList());
    }

    private List<String> changeLoggerLevel(LoggerBean loggerBean) {
        String name = loggerBean.getName();
        LogLevel logLevel = LEVELS.getOrDefault(loggerBean.getLevel(), LogLevel.INFO);
        LoggingSystem loggingSystem = SpringUtils.getBean(LoggingApplicationListener.LOGGING_SYSTEM_BEAN_NAME, LoggingSystem.class);
        //按分组设置日志级别
        LoggerGroups loggerGroups = SpringUtils.getBean(LoggingApplicationListener.LOGGER_GROUPS_BEAN_NAME, LoggerGroups.class);
        LoggerGroup group = loggerGroups.get(name);
        if (group != null && group.hasMembers()) {
            group.configureLogLevel(logLevel, loggingSystem::setLogLevel);
            List<String> loggerConfigs = group.getMembers().stream().map(loggingSystem::getLoggerConfiguration).sorted(Comparator.comparing(LoggerConfiguration::getName)).map(LoggerConfiguration::toString).collect(Collectors.toList());
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

}
