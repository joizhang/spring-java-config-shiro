package com.joizhang.imooc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author joizhang
 */
@Controller
public class ErrorController {

    @GetMapping(value = "errors")
    public ModelAndView renderErrorPage(HttpServletRequest httpRequest) {

        ModelAndView errorPage = new ModelAndView("403");
        String errorMsg = "";
        int httpErrorCode = getErrorCode(httpRequest);

        switch (httpErrorCode) {
            case 400:
                errorMsg = "Http Error Code: 400. Bad Request";
                errorPage = new ModelAndView("400");
                break;
            case 401:
                errorMsg = "Http Error Code: 401. Unauthorized";
                errorPage = new ModelAndView("401");
                break;
            case 404:
                errorMsg = "Http Error Code: 404. Resource not found";
                errorPage = new ModelAndView("404");
                break;
            case 500:
                errorMsg = "Http Error Code: 500. Internal Server Error";
                errorPage = new ModelAndView("500");
                break;
            default:
                break;
        }
        errorPage.addObject("errorMsg", errorMsg);
        return errorPage;
    }

    private int getErrorCode(HttpServletRequest httpRequest) {
        return Integer.valueOf(httpRequest.getParameter("javax.servlet.error.status_code"));
    }
}