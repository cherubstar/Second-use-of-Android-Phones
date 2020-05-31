package com.star.monitor.mapper;

import com.star.monitor.bean.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface UserMapper {

    @Insert("INSERT INTO user(account, cypher) VALUES(#{account}, #{cypher})")
    void signUp(User user);

    @Select("SELECT * FROM user WHERE account = #{account}")
    User signIn(String account);

    @Select("SELECT * FROM user WHERE account = #{account}")
    User judgePhoneIsExist(String account);
}
