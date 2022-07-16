package com.yunpan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunpan.entity.File;
import com.yunpan.entity.Share;
import com.yunpan.entity.ShareFiles;
import com.yunpan.mapper.FileMapper;
import com.yunpan.mapper.ShareFilesMapper;
import com.yunpan.mapper.ShareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/share")
public class ShareController {

    @Autowired
    private ShareMapper shareMapper;

    @Autowired
    private ShareFilesMapper shareFilesMapper;

    @Autowired
    private FileMapper fileMapper;

    @PostMapping("/share")
    public Map<Object, Object> share(@RequestBody Map<String, Object> map, HttpSession session, HttpServletRequest request) {
        Map<Object, Object> result = new HashMap<>();
        Integer user_id = (Integer) session.getAttribute("user_id");
        String password = getRandomString(4);
        Share share = new Share();
        try {
            Integer period = (Integer) map.get("period");
            List<Integer> operationList = (List<Integer>) map.get("operationList");
            share.setCreate_time(LocalDateTime.now());
            share.setCreator_id(user_id);
            share.setPassword(password);
            share.setPeriod(period);
            shareMapper.insert(share);
            for (Integer integer : operationList) {
                ShareFiles shareFiles = new ShareFiles();
                shareFiles.setShare_id(share.getId());
                shareFiles.setFile_id(integer);
                shareFilesMapper.insert(shareFiles);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            return result;
        }
        String url = request.getScheme() + "://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/share/get_share/" + share.getId();
        result.put("code", 0);
        result.put("url", url);
        result.put("password", password);
        return result;
    }

    @GetMapping("/get_share/{share_id}")
    public Object getShare(@PathVariable("share_id") Integer share_id, HttpServletRequest request, HttpSession session){
        LocalDateTime now = LocalDateTime.now();
        Share share = shareMapper.selectById(share_id);
        if(share == null || share.getCreate_time().plusMinutes(share.getPeriod()).isBefore(now)){
            return new ModelAndView("error.html");
        }
        return new ModelAndView("share.html");
    }

    @PostMapping("/get_share/{share_id}")
    public Object getShare(@PathVariable("share_id") Integer share_id, @RequestBody Map<String, String> map, HttpServletRequest request, HttpSession session){
        Share share = shareMapper.selectById(share_id);
        String url = request.getScheme() + "://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/share/share_files/" + share_id;
        Integer share_id1 = (Integer) session.getAttribute("share_id");
        Map<String, Object> result = new HashMap<>();
        if(share_id1 != null && share_id1.equals(share_id)){
            result.put("code", 0);
            result.put("url", url);
            return result;
        }
        String password = map.get("password");
        if(share != null && share.getPassword().equals(password)){
            session.setAttribute("share_id", share_id);
            result.put("code", 0);
            result.put("url", url);
            return result;
        }
        result.put("code", 1);
        result.put("error", "密码错误");
        return result;
    }

    @GetMapping("/share_files/{share_id}")
    public Object shareFiles(@PathVariable("share_id")Integer share_id, HttpSession session){
        Integer share_id1 = (Integer) session.getAttribute("share_id");
        if(share_id1 == null || !share_id1.equals(share_id)){
            return new ModelAndView("redirect:/share/get_share/" + share_id);
        }
        return new ModelAndView("share_file.html");
    }

    @PostMapping("/share_files/{share_id}")
    public Object shareFiles(@PathVariable("share_id")Integer share_id, @RequestBody Map<String, Integer> map, HttpSession session){
        List<List<Object>> bread = new ArrayList<>();
        Integer file_id = map.get("file_id");
        List<File> files;
        LambdaQueryWrapper<File> fileLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(file_id == 0){
            LambdaQueryWrapper<ShareFiles> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShareFiles::getShare_id, share_id);
            List<ShareFiles> shareFiles = shareFilesMapper.selectList(queryWrapper);
            List<Integer> list = new ArrayList<>();
            for (ShareFiles shareFile : shareFiles) {
                list.add(shareFile.getFile_id());
            }
            fileLambdaQueryWrapper.in(File::getId, list);
        }else {
            File file = fileMapper.selectById(file_id);
            if(file != null){
                List<Object> objectList = new ArrayList<>();
                objectList.add(file.getId(), file.getFilename());
                bread.add(0, objectList);
                while (file.getParent_id() != null){
                    file = fileMapper.selectById(file.getParent_id());
                    objectList = new ArrayList<>();
                    objectList.add(file.getId(), file.getFilename());
                    bread.add(0, objectList);
                }
            }
            fileLambdaQueryWrapper.eq(File::getParent_id, file_id);
            fileLambdaQueryWrapper.eq(File::getIs_delete, 0);
            fileLambdaQueryWrapper.orderByDesc(File::getFiletype);
        }
        files = fileMapper.selectList(fileLambdaQueryWrapper);
        Map<Object, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("data", files);
        result.put("bread", bread);
        return result;
    }

    @PostMapping("/get_share_files")
    public Object getShareFiles(@RequestBody Map<String, Object> map, HttpSession session){
        Map<Object, Object> result = new HashMap<>();
        Integer user_id = (Integer) session.getAttribute("user_id");
        try {
            Integer parent_id = (Integer) map.get("parent_id");
            List<Integer> operationList = (List<Integer>) map.get("operationList");
            LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(File::getId, operationList);
            queryWrapper.eq(File::getIs_delete, 0);
            List<File> files = fileMapper.selectList(queryWrapper);
            for (File file : files) {
                File file1 = new File();
                LambdaQueryWrapper<File> fileLambdaQueryWrapper = new LambdaQueryWrapper<>();
                file1.setFilename(file.getFilename());
                file1.setFile_hash_name(file.getFile_hash_name());
                file1.setFiletype(file.getFiletype());
                file1.setFilePath(file.getFilePath());
                file1.setFile_hash(file.getFile_hash());
                file1.setStatus(1);
                if(parent_id != 0){
                    file1.setParent_id(parent_id);
                    fileLambdaQueryWrapper.eq(File::getParent_id, file1.getParent_id());
                }else {
                    fileLambdaQueryWrapper.isNull(File::getParent_id);
                }
                file1.setUser_id(user_id);
                file1.setCreate_time(LocalDateTime.now());
                file1.setIs_delete(0);

                fileLambdaQueryWrapper.eq(File::getUser_id, user_id);
                fileLambdaQueryWrapper.eq(File::getFilename, file1.getFilename());
                fileLambdaQueryWrapper.eq(File::getIs_delete, 0);
                Integer count = fileMapper.selectCount(fileLambdaQueryWrapper);
                if(count > 0){
                    isRepetition(file1, user_id);
                }
                fileMapper.insert(file1);
            }
        }catch (Exception e){
            e.printStackTrace();
            result.put("code", 1);
            result.put("message", "保存失败");
            return result;
        }
        result.put("code", 0);
        result.put("message", "保存成功");
        return result;
    }

    public void isRepetition(File file, Integer user_id){
        Pattern compile = Pattern.compile("[(](\\d+)[)][.]", Pattern.CANON_EQ);
        Matcher matcher = compile.matcher(file.getFilename());
        if (matcher.find()){
            String group = matcher.group();
            String group1 = matcher.group(1);
            String group2 = group.replace(group1, String.valueOf(Integer.parseInt(group1) + 1));
            file.setFilename(file.getFilename().replace(group, group2));
        }else {
            int i = file.getFilename().lastIndexOf('.');
            if(i == -1){
                file.setFilename(file.getFilename() + "(1).dir");
            }else {
                if (i == 1) {
                    i--;
                }
                String[] split = file.getFilename().split("\\.", i);
                file.setFilename(split[0] + "(1)." + split[1]);
            }
        }
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getUser_id, user_id);
        queryWrapper.eq(File::getFilename, file.getFilename());
        queryWrapper.eq(File::getParent_id, file.getParent_id());
        queryWrapper.eq(File::getIs_delete, 0);
        File file1 = fileMapper.selectOne(queryWrapper);
        if(null != file1){
            isRepetition(file1, user_id);
        }
    }

    public String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

}
