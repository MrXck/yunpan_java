package com.yunpan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunpan.entity.Share;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

public interface ShareService extends IService<Share> {

    Map<Object, Object> share(Map<String, Object> map, HttpSession session, HttpServletRequest request);

    Object getShare(Integer share_id, HttpServletRequest request, HttpSession session);

    Object getShare(Integer share_id, Map<String, String> map, HttpServletRequest request, HttpSession session);

    Object shareFiles(Integer share_id, HttpSession session);

    Object shareFiles(Integer share_id, Map<String, Integer> map, HttpSession session);

    Object getShareFiles(Map<String, Object> map, HttpSession session);
}
