package com.dlog.spring.boot.autoconfigure;

import com.dlog.spring.boot.autoconfigure.properties.DynamicLoggerProperties;
import com.dlog.spring.boot.autoconfigure.web.DynamicLoggerAuthFilterConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties({DynamicLoggerProperties.class})
@Import({DynamicLoggerAuthFilterConfiguration.class})
public class DynamicLoggerAutoConfigure {
}
