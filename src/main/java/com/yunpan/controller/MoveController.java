package com.yunpan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunpan.entity.File;
import com.yunpan.mapper.FileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/move")
public class MoveController {

    @Autowired
    private FileMapper fileMapper;

    @PostMapping("/move")
    public Map<Object, Object> move(@RequestBody Map<Object, Object> map, HttpSession session){
        Integer user_id = (Integer) session.getAttribute("user_id");
        Map<Object, Object> result = new HashMap<>();
        try {
            List<Integer> operationList = (List<Integer>) map.get("operationList");
            Integer parent_id = (Integer) map.get("parent_id");
            File parent_obj;
            LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper<File> fileLambdaQueryWrapper = new LambdaQueryWrapper<>();
            if(parent_id == 0){
                queryWrapper.isNotNull(File::getId);
                fileLambdaQueryWrapper.isNull(File::getId);
            }else {
                queryWrapper.ne(File::getId, parent_id);
                fileLambdaQueryWrapper.eq(File::getId, parent_id);
            }
            fileLambdaQueryWrapper.eq(File::getIs_delete, 0);
            parent_obj = fileMapper.selectList(fileLambdaQueryWrapper).get(0);
            queryWrapper.eq(File::getUser_id, user_id);
            queryWrapper.eq(File::getIs_delete, 0);
            queryWrapper.in(File::getId, operationList);
            List<File> files = fileMapper.selectList(queryWrapper);
            if(files.size() > 0){
                for (File file : files) {
                    if(file.getParent_id() != null && parent_obj != null){
                        while (parent_obj.getParent_id() != null){
                            if (parent_obj.getParent_id().equals(file.getId())){
                                result.put("code", 1);
                                result.put("message", "不可以移动到自己里面");
                                return result;
                            }
                            parent_obj = fileMapper.selectById(parent_obj.getParent_id());
                        }
                    }
                    while (file.getParent_id() != null){
                        if(file.getParent_id().equals(parent_id)){
                            result.put("code", 1);
                            result.put("message", "不可以移动到自己里面");
                            return result;
                        }
                        file = fileMapper.selectById(file.getParent_id());
                    }
                }
                for (File file : files) {
                    file.setParent_id(parent_id);
                    fileMapper.updateById(file);
                }
            }else {
                result.put("code", 1);
                result.put("message", "不可以移动到自己里面");
                return result;
            }
        } catch (Exception e) {
            result.put("code", 1);
            result.put("message", "移动失败");
            return result;
        }
        result.put("code", 0);
        result.put("message", "移动成功");
        return result;
    }

    @PostMapping("/dirlist")
    public Map<Object, Object> dirlist(@RequestBody Map<String, Integer> map, HttpSession session){
        Integer user_id = (Integer) session.getAttribute("user_id");
        Map<Object, Object> result = new HashMap<>();
        Integer parent_id = map.get("parent_id");
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        List<File> files;
        List<List<Object>> bread = new ArrayList<>();
        if(parent_id == 0){
            queryWrapper.isNull(File::getParent_id);
            queryWrapper.eq(File::getUser_id, user_id);
            queryWrapper.eq(File::getFiletype, 1);
            queryWrapper.eq(File::getStatus, 1);
            queryWrapper.eq(File::getIs_delete, 0);
            files = fileMapper.selectList(queryWrapper);
        }else {
            File file = fileMapper.selectById(parent_id);
            if(null != file) {
                ArrayList<Object> list = new ArrayList<>();
                list.add(file.getId());
                list.add(file.getFilename());
                bread.add(0, list);
            }
            while (file != null && file.getParent_id() != null){
                ArrayList<Object> list = new ArrayList<>();
                file = fileMapper.selectById(file.getParent_id());
                list.add(file.getId());
                list.add(file.getFilename());
                bread.add(0, list);
            }
            queryWrapper.eq(File::getParent_id, parent_id);
            queryWrapper.eq(File::getUser_id, user_id);
            queryWrapper.eq(File::getFiletype, 1);
            queryWrapper.eq(File::getStatus, 1);
            queryWrapper.eq(File::getIs_delete, 0);
            files = fileMapper.selectList(queryWrapper);
        }
        result.put("code", 0);
        result.put("data", files);
        result.put("bread", bread);
        return result;
    }

    @GetMapping("/drag_move")
    public Map<Object, Object> dragMove(@RequestParam("operationList") List<Integer> operationList, Integer parent_id, HttpSession session){
        Integer user_id = (Integer) session.getAttribute("user_id");
        Map<Object, Object> result = new HashMap<>();
        if(operationList.size() == 0){
            result.put("code", 1);
            result.put("message", "移动失败");
            return result;
        }
        if(operationList.contains(parent_id)){
            result.put("code", 1);
            result.put("message", "不可以移动到自己里面");
            return result;
        }
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(File::getId, operationList);
        queryWrapper.eq(File::getUser_id, user_id);
        queryWrapper.eq(File::getIs_delete, 0);
        List<File> files = fileMapper.selectList(queryWrapper);
        for (File file : files) {
            file.setParent_id(parent_id);
            fileMapper.updateById(file);
        }
        result.put("code", 0);
        result.put("message", "移动成功");
        return result;
    }


}
