package com.yunpan.controller;

import com.yunpan.entity.User;
import com.yunpan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("login")
    private Map<Object, Object> login(@RequestBody User user, HttpSession session){
        return userService.login(user, session);
    }

    @PostMapping("register")
    private Map<Object, Object> register(@RequestBody HashMap<String, String> map){
        return userService.register(map);
    }

    @GetMapping("/logout")
    public Object logout(HttpSession session){
        session.removeAttribute("user_id");
        return new ModelAndView("redirect:account.html");
    }

}
