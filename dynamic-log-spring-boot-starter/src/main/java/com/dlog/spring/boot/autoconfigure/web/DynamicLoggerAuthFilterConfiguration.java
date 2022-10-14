/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dlog.spring.boot.autoconfigure.web;

import com.dlog.LoggerAuthFilter;
import com.dlog.spring.boot.autoconfigure.properties.DynamicLoggerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@ConditionalOnWebApplication
public class DynamicLoggerAuthFilterConfiguration {
    private static final String DEFAULT_ALLOW_IP = "127.0.0.1";

    @Bean
    public FilterRegistrationBean webStatFilterRegistrationBean(DynamicLoggerProperties properties) {
        DynamicLoggerProperties.LoggerAuthFilter config = properties.getLoggerAuthFilter();
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        LoggerAuthFilter filter = new LoggerAuthFilter();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns(config.getUrlPattern() != null ? config.getUrlPattern() : "/dlog/*");
        if (config.getAllow() != null) {
            registrationBean.addInitParameter("allow", config.getAllow());
        } else {
            registrationBean.addInitParameter("allow", DEFAULT_ALLOW_IP);
        }
        if (config.getDeny() != null) {
            registrationBean.addInitParameter("deny", config.getDeny());
        }
        if (config.getLoginUsername() != null) {
            registrationBean.addInitParameter("loginUsername", config.getLoginUsername());
        } else {
            registrationBean.addInitParameter("loginUsername", "admin");
        }
        if (config.getLoginPassword() != null) {
            registrationBean.addInitParameter("loginPassword", config.getLoginPassword());
        } else {
            registrationBean.addInitParameter("loginPassword", "admin123456");
        }
        return registrationBean;
    }
}
