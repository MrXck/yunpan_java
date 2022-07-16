package com.yunpan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunpan.entity.File;
import com.yunpan.mapper.FileMapper;
import com.yunpan.utils.ZipUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/file")
public class FileController {

    private final static String PATH = System.getProperty("user.dir") + "/files/";

    @Autowired
    private FileMapper fileMapper;

    @PostMapping("/file")
    public Map<Object, Object> file(@RequestBody Map<Object, Integer> map, HttpSession session){
        Map<Object, Object> result = new HashMap<>();
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        Integer file_id = map.get("file_id");
        Integer status = map.get("status");
        Integer user_id = (Integer) session.getAttribute("user_id");
        List<List<Object>> bread = new ArrayList<>();
        if(file_id != 0){
            queryWrapper.eq(File::getParent_id, file_id);
        }else {
            queryWrapper.isNull(File::getParent_id);
        }
        queryWrapper.eq(File::getStatus, status);
        queryWrapper.eq(File::getUser_id, user_id);
        queryWrapper.eq(File::getIs_delete, 0);
        queryWrapper.orderByDesc(File::getFiletype);
        List<File> files = fileMapper.selectList(queryWrapper);
        File file = fileMapper.selectById(file_id);
        if(null != file){
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

        result.put("code", 0);
        result.put("data", files);
        result.put("bread", bread);
        return result;
    }

    @GetMapping("/download/{file_id}")
    public void download(@PathVariable("file_id")Integer file_id, HttpSession session, HttpServletResponse response){
        Integer user_id = (Integer) session.getAttribute("user_id");
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getUser_id, user_id);
        queryWrapper.eq(File::getId, file_id);
        File file = fileMapper.selectOne(queryWrapper);
        try {
            FileInputStream fileInputStream = new FileInputStream(new java.io.File(file.getFilePath().replace("\u202a", "")));
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("application/octet-stream");
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
        }
    }

    @PostMapping("/create")
    public Map<Object, Object> create(@RequestBody Map<Object, Object> map, HttpSession session){
        Integer user_id = (Integer) session.getAttribute("user_id");
        Map<Object, Object> result = new HashMap<>();

        Integer file_id = (Integer) map.get("file_id");
        String dir_name = (String) map.get("dir_name");

        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        if(file_id == 0){
            queryWrapper.isNull(File::getParent_id);
        }else {
            queryWrapper.eq(File::getParent_id, file_id);
        }

        queryWrapper.eq(File::getUser_id, user_id);
        queryWrapper.eq(File::getFilename, dir_name);
        queryWrapper.eq(File::getFiletype, 1);
        queryWrapper.eq(File::getStatus, 1);
        queryWrapper.eq(File::getIs_delete, 0);

        Integer count = fileMapper.selectCount(queryWrapper);
        if(count == 0){
            File file = new File();
            file.setFilename(dir_name);
            file.setFile_hash_name("1");
            file.setFiletype(1);
            file.setFilePath("1");
            file.setFile_hash("1");
            file.setStatus(1);
            if (file_id != 0){
                file.setParent_id(file_id);
            }
            file.setUser_id(user_id);
            file.setIs_delete(0);
            file.setCreate_time(LocalDateTime.now());
            fileMapper.insert(file);
            result.put("code", 0);
            result.put("message", "创建成功");
            return result;
        }
        result.put("code", 1);
        result.put("message", "该目录下存在同名文件夹");
        return result;
    }

    @PostMapping("/delete")
    public Map<Object, Object> delete(@RequestBody Map<Object, Object> map, HttpSession session){
        Integer user_id = (Integer) session.getAttribute("user_id");
        Map<Object, Object> result = new HashMap<>();

        try {
            List<Integer> operationList = (List<Integer>) map.get("operationList");
            LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(File::getUser_id, user_id);
            queryWrapper.eq(File::getIs_delete, 0);
            queryWrapper.in(File::getId, operationList);
            List<File> files = fileMapper.selectList(queryWrapper);
            for (File file : files) {
                deleteChild(file, user_id);
            }
        } catch (Exception e) {
            result.put("code", 1);
            result.put("message", "删除失败");
            return result;
        }
        result.put("code", 0);
        result.put("message", "删除成功");
        return result;
    }

    public void deleteChild(File file, Integer user_id){
        if(file.getFiletype() == 1){
            LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(File::getUser_id, user_id);
            queryWrapper.eq(File::getIs_delete, 0);
            queryWrapper.eq(File::getParent_id, file.getId());
            List<File> files = fileMapper.selectList(queryWrapper);
            if (file.getStatus() == 0){
                file.setIs_delete(1);
            }else {
                file.setStatus(0);
            }
            fileMapper.updateById(file);
            for (File file1 : files) {
                deleteChild(file1, user_id);
            }
        }else {
            if (file.getStatus() == 0){
                file.setIs_delete(1);
            }else {
                file.setStatus(0);
            }
            fileMapper.updateById(file);
        }
    }

    @PostMapping("/search")
    public Map<Object, Object> search(@RequestBody Map<Object, Object> map, HttpSession session){
        Integer user_id = (Integer) session.getAttribute("user_id");
        Map<Object, Object> result = new HashMap<>();
        String query = (String) map.get("query");
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(File::getFilename, query);
        queryWrapper.eq(File::getUser_id, user_id);
        queryWrapper.eq(File::getStatus, 1);
        queryWrapper.eq(File::getIs_delete, 0);
        List<File> files = fileMapper.selectList(queryWrapper);
        result.put("code", 0);
        result.put("data", files);
        return result;
    }

    @GetMapping("/rename")
    public Map<Object, Object> rename(@RequestParam("operationList") List<Integer> operationList, String rename, Integer parent_id, HttpSession session){
        Integer user_id = (Integer) session.getAttribute("user_id");
        Map<Object, Object> result = new HashMap<>();

        if(null == rename && operationList.size() != 1){
            result.put("code", 1);
            result.put("message", "重命名失败");
            return result;
        }

        LambdaQueryWrapper<File> fileQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<File> dirQueryWrapper = new LambdaQueryWrapper<>();
        if(parent_id == 0){
            fileQueryWrapper.isNull(File::getParent_id);
            dirQueryWrapper.isNull(File::getParent_id);
        }else {
            fileQueryWrapper.eq(File::getParent_id, parent_id);
            dirQueryWrapper.eq(File::getParent_id, parent_id);
        }

        fileQueryWrapper.eq(File::getUser_id, user_id);
        fileQueryWrapper.eq(File::getFiletype, 0);
        fileQueryWrapper.eq(File::getStatus, 1);
        fileQueryWrapper.eq(File::getIs_delete, 0);
        List<File> files = fileMapper.selectList(fileQueryWrapper);
        boolean flag = true;
        for (File file : files) {
            if(rename != null && rename.equals(file.getFilename())){
                flag = false;
                break;
            }
        }
        if (flag){
            fileQueryWrapper.eq(File::getId, operationList.get(0));
            File file = fileMapper.selectOne(fileQueryWrapper);
            if(file != null){
                file.setFilename(rename);
                fileMapper.updateById(file);
                result.put("code", 0);
                result.put("message", "重命名成功");
                return result;
            }
        }else {
            result.put("code", 1);
            result.put("message", "该文件夹下存在相同文件名");
            return result;
        }


        dirQueryWrapper.eq(File::getUser_id, user_id);
        dirQueryWrapper.eq(File::getFiletype, 1);
        dirQueryWrapper.eq(File::getStatus, 1);
        dirQueryWrapper.eq(File::getIs_delete, 0);
        List<File> dirs = fileMapper.selectList(dirQueryWrapper);
        for (File dir : dirs) {
            if(rename != null && rename.equals(dir.getFilename())){
                flag = false;
                break;
            }
        }
        if (flag){
            dirQueryWrapper.eq(File::getId, operationList.get(0));
            File file = fileMapper.selectOne(dirQueryWrapper);
            if(file != null){
                file.setFilename(rename);
                fileMapper.updateById(file);
                result.put("code", 0);
                result.put("message", "重命名成功");
                return result;
            }
        }else {
            result.put("code", 1);
            result.put("message", "该文件夹下存在相同目录名");
            return result;
        }

        result.put("code", 0);
        result.put("message", "重命名成功");
        return result;
    }

    @GetMapping("/restore_files")
    public Map<Object, Object> restore(@RequestParam("operationList") List<Integer> operationList, HttpSession session){
        Integer user_id = (Integer) session.getAttribute("user_id");
        Map<Object, Object> result = new HashMap<>();

        if(operationList.size() < 1){
            result.put("code", 1);
            result.put("message", "恢复失败");
            return result;
        }

        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(File::getId, operationList);
        queryWrapper.eq(File::getUser_id, user_id);
        queryWrapper.eq(File::getStatus, 0);
        queryWrapper.eq(File::getIs_delete, 0);
        List<File> files = fileMapper.selectList(queryWrapper);
        restoreChild(files, user_id);


        return result;
    }


    public void restoreChild(List<File> files, Integer user_id){
        for (File file : files) {
            LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(File::getUser_id, user_id);
            if(file.getParent_id() != null){
                queryWrapper.eq(File::getParent_id, file.getParent_id());
            }else {
                queryWrapper.isNull(File::getParent_id);
            }
            queryWrapper.eq(File::getFilename, file.getFilename());
            queryWrapper.eq(File::getIs_delete, 0);
            queryWrapper.ne(File::getId, file.getId());
            List<File> fileObjList = fileMapper.selectList(queryWrapper);
            if(fileObjList.size() > 0){
                isRepetition(file, user_id);
            }
            file.setStatus(1);
            fileMapper.updateById(file);
            LambdaQueryWrapper<File> fileQueryWrapper = new LambdaQueryWrapper<>();
            fileQueryWrapper.eq(File::getUser_id, user_id);
            fileQueryWrapper.eq(File::getParent_id, file.getId());
            fileQueryWrapper.eq(File::getStatus, 0);
            fileQueryWrapper.eq(File::getIs_delete, 0);
            List<File> fileList = fileMapper.selectList(fileQueryWrapper);
            if (fileList.size() > 0){
                restoreChild(fileList, user_id);
            }
        }
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

    @GetMapping("/download_files")
    public void downloadFiles(@RequestParam("operationList") List<Integer> operationList, HttpSession session, HttpServletResponse response) {
        Integer user_id = (Integer) session.getAttribute("user_id");
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getUser_id, user_id);
        queryWrapper.eq(File::getStatus, 1);
        queryWrapper.in(File::getId, operationList);

        List<File> files = fileMapper.selectList(queryWrapper);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=");
        try {
            ZipUtils.downloadZip(response.getOutputStream(), files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}



