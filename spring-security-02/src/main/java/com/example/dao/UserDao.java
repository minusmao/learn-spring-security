package com.example.dao;

import com.example.entity.Role;
import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserDao {

    //根据用户名查询用户
    User loadUserByUsername(String username);

    //根据用户 id 查询角色
    List<Role> getRolesByUid(Integer uid);

}
