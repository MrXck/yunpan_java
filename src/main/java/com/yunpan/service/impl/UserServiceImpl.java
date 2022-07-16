package com.yunpan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunpan.entity.User;
import com.yunpan.mapper.UserMapper;
import com.yunpan.service.UserService;
import com.yunpan.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;


    @Override
    public Map<Object, Object> login(User user, HttpSession session) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        queryWrapper.eq(User::getPassword, MD5Utils.md5(user.getPassword()));
        Map<Object, Object> map = new HashMap<>();
        User user1 = userMapper.selectOne(queryWrapper);
        if(null != user1){
            map.put("code", 0);
            map.put("to", "/");
            session.setAttribute("user_id", user1.getId());
            return map;
        }
        List<String> list = new ArrayList<>();
        list.add("用户名或密码错误");
        map.put("code", 1);
        map.put("errors", list);
        return map;
    }

    @Override
    public Map<Object, Object> register(HashMap<String, String> map) {
        Map<Object, Object> result = new HashMap<>();

        String password = map.get("password");
        String confirm_password = map.get("confirm_password");
        String username = map.get("username");
        List<String> list = new ArrayList<>();
        if(password != null && password.equals(confirm_password)){
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, username);
            Integer count = userMapper.selectCount(queryWrapper);
            if(count > 0){
                list.add("用户名已存在");
                result.put("code", 1);
                result.put("errors", list);
                return result;
            }
            User user = new User();
            user.setUsername(username);
            user.setPassword(MD5Utils.md5(password));
            userMapper.insert(user);
            result.put("code", 0);
            result.put("to", "/account.html");
            return result;
        }
        list.add("两次输入的密码不一致");
        result.put("code", 1);
        result.put("errors", list);
        return result;
    }
}
