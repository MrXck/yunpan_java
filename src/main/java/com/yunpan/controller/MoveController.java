package com.yunpan.controller;

import com.yunpan.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/move")
public class MoveController {


    @Autowired
    private FileService fileService;

    @PostMapping("/move")
    public Map<Object, Object> move(@RequestBody Map<Object, Object> map, HttpSession session){
        return fileService.move(map, session);
    }

    @PostMapping("/dirlist")
    public Map<Object, Object> dirlist(@RequestBody Map<String, Integer> map, HttpSession session){
        return fileService.dirlist(map, session);
    }

    @GetMapping("/drag_move")
    public Map<Object, Object> dragMove(@RequestParam("operationList") List<Integer> operationList, Integer parent_id, HttpSession session){
        return fileService.dragMove(operationList, parent_id, session);
    }


}
