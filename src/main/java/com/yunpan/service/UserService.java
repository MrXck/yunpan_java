package com.yunpan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunpan.entity.User;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public interface UserService extends IService<User> {

    Map<Object, Object> login(User user, HttpSession session);

    Map<Object, Object> register(HashMap<String, String> map);
}
