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
package com.dlog;

import com.dlog.util.DruidWebUtils;
import com.dlog.util.PatternMatcher;
import com.dlog.util.ServletPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@WebFilter("/*")
public class WebStatFilter implements Filter {
//    private static final Log LOG = LogFactory.getLog(WebStatFilter.class);

    public static final String PARAM_NAME_PROFILE_ENABLE = "profileEnable";
    public static final String PARAM_NAME_SESSION_STAT_ENABLE = "sessionStatEnable";
    public static final String PARAM_NAME_SESSION_STAT_MAX_COUNT = "sessionStatMaxCount";
    public static final String PARAM_NAME_EXCLUSIONS = "exclusions";
    public static final String PARAM_NAME_PRINCIPAL_SESSION_NAME = "principalSessionName";
    public static final String PARAM_NAME_PRINCIPAL_COOKIE_NAME = "principalCookieName";
    public static final String PARAM_NAME_REAL_IP_HEADER = "realIpHeader";

    /**
     * PatternMatcher used in determining which paths to react to for a given request.
     */
    protected PatternMatcher pathMatcher = new ServletPathMatcher();

    private Set<String> excludesPattern;

    protected String contextPath;

    public String getContextPath() {
        return contextPath;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        StatHttpServletResponseWrapper responseWrapper = new StatHttpServletResponseWrapper(httpResponse);

        String requestURI = httpRequest.getRequestURI();

        if (isExclusion(requestURI)) {
            chain.doFilter(request, response);
            return;
        }
        Throwable error = null;
        try {
            chain.doFilter(request, responseWrapper);
        } catch (IOException e) {
            error = e;
            throw e;
        } catch (ServletException e) {
            error = e;
            throw e;
        } catch (RuntimeException e) {
            error = e;
            throw e;
        } catch (Error e) {
            error = e;
            throw e;
        } finally {

        }
    }

    public boolean isExclusion(String requestURI) {
        if (excludesPattern == null || requestURI == null) {
            return false;
        }

        if (contextPath != null && requestURI.startsWith(contextPath)) {
            requestURI = requestURI.substring(contextPath.length());
            if (!requestURI.startsWith("/")) {
                requestURI = "/" + requestURI;
            }
        }

        for (String pattern : excludesPattern) {
            if (pathMatcher.matches(pattern, requestURI)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        {
//            String exclusions = config.getInitParameter(PARAM_NAME_EXCLUSIONS);
            String exclusions = "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/dlog/*";
            if (exclusions != null && exclusions.trim().length() != 0) {
                excludesPattern = new HashSet<String>(Arrays.asList(exclusions.split("\\s*,\\s*")));
            }
        }
        this.contextPath = DruidWebUtils.getContextPath(config.getServletContext());
    }

    @Override
    public void destroy() {
    }

    public static final class StatHttpServletResponseWrapper extends HttpServletResponseWrapper implements HttpServletResponse {
        //初始值应该设置为：HttpServletResponse.SC_OK，而不是 0。
        private int status = HttpServletResponse.SC_OK;

        public StatHttpServletResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        public void setStatus(int statusCode) {
            super.setStatus(statusCode);
            this.status = statusCode;
        }

        @SuppressWarnings("deprecation")
        public void setStatus(int statusCode, String statusMessage) {
            super.setStatus(statusCode, statusMessage);
            this.status = statusCode;
        }

        public void sendError(int statusCode, String statusMessage) throws IOException {
            super.sendError(statusCode, statusMessage);
            this.status = statusCode;
        }

        public void sendError(int statusCode) throws IOException {
            super.sendError(statusCode);
            this.status = statusCode;
        }

        public int getStatus() {
            return status;
        }
    }
}
