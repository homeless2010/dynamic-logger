package com.dlog.spring.boot.autoconfigure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.dynamic.logger")
public class DynamicLoggerProperties {
    private LoggerAuthFilter loggerAuthFilter = new LoggerAuthFilter();

    public LoggerAuthFilter getLoggerAuthFilter() {
        return loggerAuthFilter;
    }

    public void setLoggerAuthFilter(LoggerAuthFilter loggerAuthFilter) {
        this.loggerAuthFilter = loggerAuthFilter;
    }

    public static class LoggerAuthFilter {
        private String urlPattern;
        private String allow;
        private String deny;
        private String loginUsername;
        private String loginPassword;

        public String getUrlPattern() {
            return urlPattern;
        }

        public void setUrlPattern(String urlPattern) {
            this.urlPattern = urlPattern;
        }

        public String getAllow() {
            return allow;
        }

        public void setAllow(String allow) {
            this.allow = allow;
        }

        public String getDeny() {
            return deny;
        }

        public void setDeny(String deny) {
            this.deny = deny;
        }

        public String getLoginUsername() {
            return loginUsername;
        }

        public void setLoginUsername(String loginUsername) {
            this.loginUsername = loginUsername;
        }

        public String getLoginPassword() {
            return loginPassword;
        }

        public void setLoginPassword(String loginPassword) {
            this.loginPassword = loginPassword;
        }
    }
}
