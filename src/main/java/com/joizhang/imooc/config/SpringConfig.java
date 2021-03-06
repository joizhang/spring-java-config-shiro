package com.joizhang.imooc.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * 相当于spring.xml
 *
 * @author joizhang
 */
@Configuration
@ComponentScan(basePackages = "com.joizhang.imooc",
        excludeFilters =
                {
                        @ComponentScan.Filter(type = FilterType.ANNOTATION, value = {EnableWebMvc.class, ControllerAdvice.class, Controller.class})
                })
public class SpringConfig {
}
