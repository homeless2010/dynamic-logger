package com.dlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class DynamicLoggerApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(DynamicLoggerApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ   dynamic-logger启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                "   d                                \n" +
                "                                    \n" +
                "   d                                \n" +
                "                                    \n" +
                "                                    \n" +
                "   d                                \n" +
                "                                    \n" +
                "                                    \n" +
                "   d                                ");
    }
}
