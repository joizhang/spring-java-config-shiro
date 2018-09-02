package com.joizhang.imooc.controller;

import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author joizhang
 */
@Controller
public class GreetingController {

    @RequestMapping("/hello")
    @ResponseBody
    public String hello(boolean error) {
        if (error) {
            throw new UnauthorizedException();
        } else {
            return "hello";
        }
    }

}
