package com.yunpan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunpan.entity.File;
import com.yunpan.mapper.FileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private final static String PATH = System.getProperty("user.dir") + "/files/";

    @Autowired
    private FileMapper fileMapper;


    @GetMapping("/upload_already")
    public Map<Object, Object> upload_already(String HASH, String filename, String file_hash, Integer parent_id, HttpSession session){
        Integer user_id = (Integer) session.getAttribute("user_id");
        Map<Object, Object> result = new HashMap<>();

        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        if(parent_id == 0){
            queryWrapper.isNull(File::getParent_id);
        }else {
            queryWrapper.eq(File::getParent_id, parent_id);
        }
        queryWrapper.eq(File::getUser_id, user_id);
        queryWrapper.eq(File::getFiletype, 0);
        queryWrapper.eq(File::getFilename, filename);
        queryWrapper.eq(File::getStatus, 1);
        queryWrapper.eq(File::getIs_delete, 0);
        List<File> files = fileMapper.selectList(queryWrapper);
        if(files.size() > 0){
            result.put("code", 1);
            result.put("message", "该目录下存在相同文件");
            return result;
        }
        LambdaQueryWrapper<File> fileQueryWrapper = new LambdaQueryWrapper<>();
        fileQueryWrapper.eq(File::getFiletype, 0);
        fileQueryWrapper.eq(File::getFile_hash, file_hash);
        List<File> fileList = fileMapper.selectList(fileQueryWrapper);
        if(fileList.size() > 0){
            File file = fileList.get(0);
            File file1 = new File();
            file1.setFilename(file.getFilename());
            file1.setFile_hash_name(file.getFile_hash_name());
            file1.setFiletype(file.getFiletype());
            file1.setFilePath(file.getFilePath());
            file1.setFile_hash(file.getFile_hash());
            file1.setStatus(1);
            if(parent_id != 0){
                file1.setParent_id(parent_id);
            }
            file1.setUser_id(user_id);
            file1.setCreate_time(LocalDateTime.now());
            file1.setIs_delete(0);
            fileMapper.insert(file1);
            result.put("code", 2);
            result.put("message", "上传成功");
            return result;
        }
        if(null == HASH){
            result.put("code", 0);
            return result;
        }
        String dirPath = PATH + "/" + HASH;
        java.io.File file = new java.io.File(dirPath);
        List<Object> list = new ArrayList<>();
        if(!file.exists()){
            file.mkdirs();
        }else {
            java.io.File[] files1 = file.listFiles();
            if(files1 != null){
                for (java.io.File file1 : files1) {
                    list.add(file1.getName());
                }
            }
        }
        result.put("code", 0);
        result.put("fileList", list);
        return result;
    }

    @PostMapping("/upload")
    public Map<Object, Object> upload(MultipartFile file, String hash, String file_hash, Integer parent_id, HttpSession session){
        Integer user_id = (Integer) session.getAttribute("user_id");
        Map<Object, Object> result = new HashMap<>();
        String HASH = String.valueOf(UUID.randomUUID());
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        if(parent_id == 0){
            queryWrapper.isNull(File::getParent_id);
        }else {
            queryWrapper.eq(File::getParent_id, parent_id);
        }
        String[] split = file.getOriginalFilename().split("\\.");
        String suffix = split[split.length - 1];
        String path = PATH + "/" + HASH + "." + suffix;
        queryWrapper.eq(File::getFilename, file.getOriginalFilename());
        queryWrapper.eq(File::getFiletype, 0);
        queryWrapper.eq(File::getUser_id, user_id);
        queryWrapper.eq(File::getIs_delete, 0);
        List<File> files = fileMapper.selectList(queryWrapper);
        if(files.size() > 0){
            result.put("code", 1);
            result.put("message", "该目录下存在相同文件");
            return result;
        }
        File file1 = new File();
        file1.setFilename(file.getOriginalFilename());
        file1.setFile_hash_name(HASH);
        file1.setFiletype(0);
        file1.setFilePath(path);
        file1.setFile_hash(file_hash);
        file1.setStatus(1);
        if(parent_id != 0){
            file1.setParent_id(parent_id);
        }
        file1.setUser_id(user_id);
        file1.setCreate_time(LocalDateTime.now());
        file1.setIs_delete(0);
        fileMapper.insert(file1);
        java.io.File f = new java.io.File(path);
        if(!f.getParentFile().exists()){
            f.getParentFile().mkdirs();
        }
        try{
            file.transferTo(f);
        }catch (Exception e){
            e.printStackTrace();
            result.put("code", 1);
            result.put("message", "上传失败");
            return result;
        }
        result.put("code", 0);
        result.put("message", "上传成功");
        return result;
    }

    @PostMapping("/upload_chunk")
    public Map<Object, Object> uploadChunk(@RequestParam Map<String, MultipartFile> map){
        Map<Object, Object> result = new HashMap<>();
        String filename = (String) map.keySet().toArray()[0];
        String dir_path = PATH + filename.split("_")[0];
        java.io.File file1 = new java.io.File(dir_path);
        try {
            if(!file1.exists()){
                file1.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String filePath = dir_path + "/" + filename;
        MultipartFile multipartFile = map.get(filename);
        try {
            multipartFile.transferTo(new java.io.File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.put("code", 0);
        return result;
    }

    @GetMapping("/upload_merge")
    public Map<Object, Object> uploadMerge(String HASH, Integer count, Integer parent_id, String filename, String file_hash, HttpSession session){
        Integer user_id = (Integer) session.getAttribute("user_id");
        Map<Object, Object> result = new HashMap<>();

        String uuid_name = String.valueOf(UUID.randomUUID());
        String file_hash_path = PATH + "/" + HASH;
        String[] split = filename.split("\\.");
        String file_path = PATH + "/" + uuid_name + "." + split[split.length - 1];
        java.io.File[] files = new java.io.File(file_hash_path).listFiles();
        String[] split1 = files[0].getName().split("\\.");
        String suffix = "." + split1[split1.length - 1];
        FileInputStream fileInputStream = null;
        try {
            new java.io.File(file_path).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(file_path)) {
            for (int i = 1; i < count + 1; i++) {
                String mergeFilename = HASH + "_" + i + suffix;
                String merge_filepath = file_hash_path + "/" + mergeFilename;
                fileInputStream = new FileInputStream(merge_filepath);
                byte[] buf = new byte[1024];
                int len;
                while ((len = fileInputStream.read(buf)) != -1){
                    fileOutputStream.write(buf, 0, len);
                    fileOutputStream.flush();
                }
                fileInputStream.close();
                new java.io.File(merge_filepath).delete();
            }
            new java.io.File(file_hash_path).delete();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (fileInputStream != null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        File file1 = new File();
        file1.setFilename(filename);
        file1.setFile_hash_name(uuid_name);
        file1.setFiletype(0);
        file1.setFilePath(file_path);
        file1.setFile_hash(file_hash);
        file1.setStatus(1);
        if(parent_id != 0){
            file1.setParent_id(parent_id);
        }
        file1.setUser_id(user_id);
        file1.setCreate_time(LocalDateTime.now());
        file1.setIs_delete(0);
        fileMapper.insert(file1);
        result.put("code", 0);
        result.put("message", "上传成功");
        return result;
    }


}
