package com.star.monitor.service;

import com.star.monitor.bean.User;
import com.star.monitor.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 判断号码是否存在
     */
    public String judgePhoneIsExist(String account){

        User user = userMapper.judgePhoneIsExist(account);

        if(user != null){
            return "1006";
        }

        return "1007";
    }

    /**
     * 注册
     */
    public String signUp(String account, String password){

        User user = new User();
        user.setAccount(account);
        user.setCypher(password);

        userMapper.signUp(user);

        return "1001";
    }


    /**
     * 登陆
     */
    public String signIn(String account, String password){

        User user = userMapper.signIn(account);
        if(user == null){
            return "1002";
        }
        if(!password.equals(user.getCypher())){
            return "1003";
        }

        return "1000";
    }
}
