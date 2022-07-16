package com.yunpan.controller;

import com.yunpan.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/share")
public class ShareController {

    @Autowired
    private ShareService shareService;

    @PostMapping("/share")
    public Map<Object, Object> share(@RequestBody Map<String, Object> map, HttpSession session, HttpServletRequest request) {
        return shareService.share(map, session, request);
    }

    @GetMapping("/get_share/{share_id}")
    public Object getShare(@PathVariable("share_id") Integer share_id, HttpServletRequest request, HttpSession session){
        return shareService.getShare(share_id, request, session);
    }

    @PostMapping("/get_share/{share_id}")
    public Object getShare(@PathVariable("share_id") Integer share_id, @RequestBody Map<String, String> map, HttpServletRequest request, HttpSession session){
        return shareService.getShare(share_id, map, request, session);
    }

    @GetMapping("/share_files/{share_id}")
    public Object shareFiles(@PathVariable("share_id")Integer share_id, HttpSession session){
        return shareService.shareFiles(share_id, session);
    }

    @PostMapping("/share_files/{share_id}")
    public Object shareFiles(@PathVariable("share_id")Integer share_id, @RequestBody Map<String, Integer> map, HttpSession session){
        return shareService.shareFiles(share_id, map, session);
    }

    @PostMapping("/get_share_files")
    public Object getShareFiles(@RequestBody Map<String, Object> map, HttpSession session){
        return shareService.getShareFiles(map, session);
    }



}
