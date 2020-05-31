/**
 * 工作单位：IT创新工作室
 * 文件名：SpringBootStartApplication
 * 作者：cheru
 * 日期：2019/10/15 7:09
 */
package com.star.monitor;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class SpringBootStartApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MonitorApplication.class);
    }
}
