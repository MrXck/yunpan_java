package com.yunpan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunpan.entity.User;
import com.yunpan.mapper.UserMapper;
import com.yunpan.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
