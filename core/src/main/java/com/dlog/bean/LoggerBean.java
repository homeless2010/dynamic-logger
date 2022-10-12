package com.dlog.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoggerBean implements Serializable {
    private static final long serialVersionUID = -5603077155885978439L;
    private String name;
    /**
     * 日志等级 参照 LogLevelConstant
     */
    private String level;
}
