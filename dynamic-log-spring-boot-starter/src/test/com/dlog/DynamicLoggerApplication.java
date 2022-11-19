package com.dlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DynamicLoggerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DynamicLoggerApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  --------------------   ლ(´ڡ`ლ)ﾞ\n" +
        "(♥◠‿◠)ﾉﾞ  dynamic-logger启动成功  ლ(´ڡ`ლ)ﾞ\n" +
        "(♥◠‿◠)ﾉﾞ  --------------------   ლ(´ڡ`ლ)ﾞ");
    }
}