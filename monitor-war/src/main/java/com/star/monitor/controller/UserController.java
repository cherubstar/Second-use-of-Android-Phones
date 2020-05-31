package com.star.monitor.controller;

import com.star.monitor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/hello")
    public String hello(){
        return "hello 星哥";
    }

    /**
     * 判断号码是否存在
     */
    @GetMapping("/judge")
    public String judgePhoneIsExist(String account){

        String result = userService.judgePhoneIsExist(account);

        return result;
    }

    /**
     * 注册
     */
    @GetMapping("/signUp")
    public String signUp(String account, String password){

        if("".equals(account)){
            return "1004";
        }
        if("".equals(password)){
            return "1005";
        }

        String result = userService.signUp(account, password);

        return result;
    }

    /**
     * 登陆
     */
    @GetMapping("/signIn")
    public String signIn(String account, String password){

        if("".equals(account)){
            return "1004";
        }
        if("".equals(password)){
            return "1005";
        }

        String result = userService.signIn(account, password);

        return result;
    }
}
