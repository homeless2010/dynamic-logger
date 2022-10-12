package com.step.iot;

import com.step.iot.util.IPAddress;
import com.step.iot.util.IPRange;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@WebFilter("/*")
public class LoggerAuthFilter implements Filter {
    public static final String SESSION_USER_KEY = "logger-user";
    public static final String PARAM_NAME_USERNAME = "loginUsername";
    public static final String PARAM_NAME_PASSWORD = "loginPassword";

    public static final String PARAM_NAME_ALLOW = "allow";
    public static final String PARAM_NAME_DENY = "deny";
    protected List<IPRange> allowList = new ArrayList<IPRange>();
    protected List<IPRange> denyList = new ArrayList<IPRange>();

    protected String username;
    protected String password;

    protected FilterConfig fc;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.fc = filterConfig;
        initAuthEnv();
    }

    private void initAuthEnv() {
        String paramUserName = fc.getInitParameter(PARAM_NAME_USERNAME);
        if (StringUtils.isNotBlank(paramUserName)) {
            this.username = paramUserName;
        }

        String paramPassword = fc.getInitParameter(PARAM_NAME_PASSWORD);
        if (!StringUtils.isEmpty(paramPassword)) {
            this.password = paramPassword;
        }

        try {
            String param = fc.getInitParameter(PARAM_NAME_ALLOW);
            if (param != null && param.trim().length() != 0) {
                param = param.trim();
                String[] items = param.split(",");

                for (String item : items) {
                    if (item == null || item.length() == 0) {
                        continue;
                    }

                    IPRange ipRange = new IPRange(item);
                    this.allowList.add(ipRange);
                }
            }
        } catch (Exception e) {
            String msg = "initParameter config error, allow : " + fc.getInitParameter(PARAM_NAME_ALLOW);
            log.error(msg, e);
        }

        try {
            String param = fc.getInitParameter(PARAM_NAME_DENY);
            if (param != null && param.trim().length() != 0) {
                param = param.trim();
                String[] items = param.split(",");

                for (String item : items) {
                    if (item == null || item.length() == 0) {
                        continue;
                    }

                    IPRange ipRange = new IPRange(item);
                    this.denyList.add(ipRange);
                }
            }
        } catch (Exception e) {
            String msg = "initParameter config error, deny : " + fc.getInitParameter(PARAM_NAME_DENY);
            log.error(msg, e);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        String requestURI = request.getRequestURI();
        response.setCharacterEncoding("utf-8");
        if (contextPath == null) { // root context
            contextPath = "";
        }
        String uri = contextPath + servletPath;
        String path = requestURI.substring(contextPath.length() + servletPath.length());
        if (!isPermittedRequest(request)) {
            path = "/nopermit.html";
            response.sendRedirect(path);
            return;
        }

        if ("/submitLogin".equals(path)) {
            String usernameParam = request.getParameter(PARAM_NAME_USERNAME);
            String passwordParam = request.getParameter(PARAM_NAME_PASSWORD);
            if (username.equals(usernameParam) && password.equals(passwordParam)) {
                request.getSession().setAttribute(SESSION_USER_KEY, username);
                response.getWriter().print("success");
            } else {
                response.getWriter().print("error");
            }
            return;
        }

        if (isRequireAuth() //
                && !containsUser(request)//
                && !checkLoginParam(request)//
                && !("/login.html".equals(path) //
                || path.startsWith("/css")//
                || path.startsWith("/js") //
                || path.startsWith("/img"))) {
            if (contextPath.equals("") || contextPath.equals("/")) {
                response.sendRedirect("/druid/login.html");
            } else {
                if ("".equals(path)) {
                    response.sendRedirect("druid/login.html");
                } else {
                    response.sendRedirect("login.html");
                }
            }
            return;
        }

        if ("".equals(path) || "/".equals(path)) {
            response.sendRedirect("/index.html");
            return;
        }

        filterChain.doFilter(request, response);
    }

    public boolean isPermittedRequest(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        boolean ipV6 = remoteAddress != null && remoteAddress.indexOf(':') != -1;

        if (ipV6) {
            return "0:0:0:0:0:0:0:1".equals(remoteAddress) || (denyList.isEmpty() && allowList.isEmpty());
        }

        IPAddress ipAddress = new IPAddress(remoteAddress);

        for (IPRange range : denyList) {
            if (range.isIPAddressInRange(ipAddress)) {
                return false;
            }
        }

        if (allowList.size() > 0) {
            for (IPRange range : allowList) {
                if (range.isIPAddressInRange(ipAddress)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    public boolean isRequireAuth() {
        return username != null;
    }

    public boolean containsUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(SESSION_USER_KEY) != null;
    }

    public boolean checkLoginParam(HttpServletRequest request) {
        String usernameParam = request.getParameter(PARAM_NAME_USERNAME);
        String passwordParam = request.getParameter(PARAM_NAME_PASSWORD);
        if (null == username || null == password) {
            return false;
        } else if (username.equals(usernameParam) && password.equals(passwordParam)) {
            return true;
        }
        return false;
    }
}
