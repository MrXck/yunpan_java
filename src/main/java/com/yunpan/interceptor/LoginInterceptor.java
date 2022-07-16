package com.yunpan.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getRequestURI();
        if(url.contains("account.html") || url.endsWith("user/login") || url.endsWith("user/register")) {
            return true;
        }
        HttpSession session = request.getSession();
        Integer user_id = (Integer) session.getAttribute("user_id");
        if(user_id != null) {
            return true;
        }else{
            response.sendRedirect("/account.html");
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        HttpSession session = request.getSession();
//        User user = (User) session.getAttribute("user");
//        if(user != null){
//            modelAndView.addObject("user", user);
//        }
//        else {
//            modelAndView.addObject("user", null);
//        }
    }

    @Override
    // 在流程都执行完毕后执行
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}