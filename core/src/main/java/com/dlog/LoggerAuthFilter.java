package com.dlog;

import com.step.iot.util.IPAddress;
import com.step.iot.util.IPRange;
import com.step.iot.util.Utils;
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
@WebFilter("/dlog/*")
public class LoggerAuthFilter implements Filter {
    public static final String SESSION_USER_KEY = "dlog-user";
    public static final String PARAM_NAME_USERNAME = "loginUsername";
    public static final String PARAM_NAME_PASSWORD = "loginPassword";
    public static final String PARAM_NAME_ALLOW = "allow";
    public static final String PARAM_NAME_DENY = "deny";
    public static final String PARAM_REMOTE_ADDR = "remoteAddress";
    public static final String PARAM_NAME_PATH = "path";
    private String servletPath = "/dlog";
    private String resourcePath = "dlog";

    private ResourceHandler handler;

    @Override
    public void init(FilterConfig config) throws ServletException {
        if (config == null) {
            return;
        }

        String path = config.getInitParameter(PARAM_NAME_PATH);
        if (path != null && !path.isEmpty()) {
            this.servletPath = path;
        }

        handler = new ResourceHandler(resourcePath);

        String paramUserName = config.getInitParameter(PARAM_NAME_USERNAME);
        if (!StringUtils.isEmpty(paramUserName)) {
            handler.username = paramUserName;
        }

        String paramPassword = config.getInitParameter(PARAM_NAME_PASSWORD);
        if (!StringUtils.isEmpty(paramPassword)) {
            handler.password = paramPassword;
        }

        String paramRemoteAddressHeader = config.getInitParameter(PARAM_REMOTE_ADDR);
        if (!StringUtils.isEmpty(paramRemoteAddressHeader)) {
            handler.remoteAddressHeader = paramRemoteAddressHeader;
        }

        try {
            String param = config.getInitParameter(PARAM_NAME_ALLOW);
            if (param != null && param.trim().length() != 0) {
                param = param.trim();
                String[] items = param.split(",");

                for (String item : items) {
                    if (item == null || item.length() == 0) {
                        continue;
                    }

                    IPRange ipRange = new IPRange(item);
                    handler.allowList.add(ipRange);
                }
            }
        } catch (Exception e) {
            String msg = "initParameter config error, allow : " + config.getInitParameter(PARAM_NAME_ALLOW);
            log.error(msg, e);
        }

        try {
            String param = config.getInitParameter(PARAM_NAME_DENY);
            if (param != null && param.trim().length() != 0) {
                param = param.trim();
                String[] items = param.split(",");

                for (String item : items) {
                    if (item == null || item.length() == 0) {
                        continue;
                    }

                    IPRange ipRange = new IPRange(item);
                    handler.denyList.add(ipRange);
                }
            }
        } catch (Exception e) {
            String msg = "initParameter config error, deny : " + config.getInitParameter(PARAM_NAME_DENY);
            log.error(msg, e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;
        String contextPath = ((HttpServletRequest) request).getContextPath();

        String requestURI = httpReq.getRequestURI();
        if (!contextPath.equals("")) {
            requestURI = requestURI.substring(((HttpServletRequest) request).getContextPath().length());
        }
        if (requestURI.equals(servletPath)) {
            httpResp.sendRedirect(httpReq.getRequestURI() + '/');
            return;
        }

        handler.service(httpReq, httpResp, servletPath);
    }

    @Override
    public void destroy() {
    }

    public static class ResourceHandler {
        protected String username;
        protected String password;

        protected List<IPRange> allowList = new ArrayList<IPRange>();
        protected List<IPRange> denyList = new ArrayList<IPRange>();

        protected String resourcePath;

        protected String remoteAddressHeader;

        public ResourceHandler(String resourcePath) {
            this.resourcePath = resourcePath;
        }

        protected void returnResourceFile(String fileName, String uri, HttpServletResponse response) throws ServletException, IOException {
            String filePath = getFilePath(fileName);

            if (filePath.endsWith(".html")) {
                response.setContentType("text/html; charset=utf-8");
            }
            if (fileName.endsWith(".jpg")) {
                byte[] bytes = Utils.readByteArrayFromResource(filePath);
                if (bytes != null) {
                    response.getOutputStream().write(bytes);
                }

                return;
            }

            String text = Utils.readFromResource(filePath);
            if (text == null) {
                return;
            }

            if (fileName.endsWith(".css")) {
                response.setContentType("text/css;charset=utf-8");
            } else if (fileName.endsWith(".js")) {
                response.setContentType("text/javascript;charset=utf-8");
            }
            response.getWriter().write(text);
        }

        protected String getFilePath(String fileName) {
            return resourcePath + fileName;
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

        protected String getRemoteAddress(HttpServletRequest request) {
            String remoteAddress = null;

            if (remoteAddressHeader != null) {
                remoteAddress = request.getHeader(remoteAddressHeader);
            }

            if (remoteAddress == null) {
                remoteAddress = request.getRemoteAddr();
            }

            return remoteAddress;
        }

        public boolean containsUser(HttpServletRequest request) {
            HttpSession session = request.getSession(false);
            return session != null && session.getAttribute(SESSION_USER_KEY) != null;
        }

        public boolean isRequireAuth() {
            return username != null;
        }

        public boolean isPermittedRequest(HttpServletRequest request) {
            String remoteAddress = getRemoteAddress(request);
            return isPermittedRequest(remoteAddress);
        }

        public boolean isPermittedRequest(String remoteAddress) {
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

        public void service(HttpServletRequest request, HttpServletResponse response, String servletPath) throws ServletException, IOException {
            String contextPath = request.getContextPath();
            String requestURI = request.getRequestURI();

            response.setCharacterEncoding("utf-8");

            if (contextPath == null) { // root context
                contextPath = "";
            }
            String uri = contextPath + servletPath;
            String path = requestURI.substring(contextPath.length() + servletPath.length());

            if (!isPermittedRequest(request)) {
                path = "/nopermit.html";
                returnResourceFile(path, uri, response);
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
                    response.sendRedirect("/dlog/login.html");
                } else {
                    if ("".equals(path)) {
                        response.sendRedirect("dlog/login.html");
                    } else {
                        response.sendRedirect("login.html");
                    }
                }
                return;
            }

            if ("".equals(path) || "/".equals(path)) {
                returnResourceFile("/index.html", uri, response);
                return;
            }
            returnResourceFile(path, uri, response);
        }
    }
}
