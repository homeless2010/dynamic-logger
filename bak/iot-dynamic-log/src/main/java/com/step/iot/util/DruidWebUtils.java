package com.step.iot.util;

import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class DruidWebUtils {
    public static String getRemoteAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && !isValidAddress(ip)) {
            ip = null;
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            if (ip != null && !isValidAddress(ip)) {
                ip = null;
            }
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
            if (ip != null && !isValidAddress(ip)) {
                ip = null;
            }
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip != null && !isValidAddress(ip)) {
                ip = null;
            }
        }

        return ip;
    }

    private static boolean isValidAddress(String ip) {
        if (ip == null) {
            return false;
        }

        for (int i = 0; i < ip.length(); ++i) {
            char ch = ip.charAt(i);
            if (ch >= '0' && ch <= '9') {
                // ignored
            } else if (ch >= 'A' && ch <= 'F') {
                // ignored
            } else if (ch >= 'a' && ch <= 'f') {
                // ignored
            } else if (ch == '.' || ch == ':') {
                //
            } else {
                return false;
            }
        }

        return true;
    }

    private static String getContextPath_2_5(ServletContext context) {
        String contextPath = context.getContextPath();

        if (contextPath == null || contextPath.length() == 0) {
            contextPath = "/";
        }

        return contextPath;
    }

    public static String getContextPath(ServletContext context) {
        if (context.getMajorVersion() == 2 && context.getMinorVersion() < 5) {
            return null;
        }

        try {
            return getContextPath_2_5(context);
        } catch (NoSuchMethodError error) {
            return null;
        }
    }

    public static Boolean getBoolean(GenericServlet servlet, String key) {
        String property = servlet.getInitParameter(key);
        if ("true".equals(property)) {
            return Boolean.TRUE;
        } else if ("false".equals(property)) {
            return Boolean.FALSE;
        }
        return null;
    }
}
