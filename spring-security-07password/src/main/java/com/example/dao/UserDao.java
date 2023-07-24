package com.example.dao;

import com.example.entity.Role;
import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserDao {

    //1.根据用户 id 获取用户角色方法
    List<Role> getRolesByUid(Integer uid);

    //2.根据用户名查询用户方法
    User loadUserByUsername(String username);

    //3.根据用户名更新密码方法
    Integer updatePassword(@Param("username") String username, @Param("password") String password);

}