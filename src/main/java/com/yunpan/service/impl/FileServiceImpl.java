package com.yunpan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunpan.entity.File;
import com.yunpan.mapper.FileMapper;
import com.yunpan.service.FileService;
import com.yunpan.utils.ZipUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    private final static String PATH = System.getProperty("user.dir") + "/files/";

    @Autowired
    private FileMapper fileMapper;

    public Map<Object, Object> file(Map<Object, Integer> map, HttpSession session){
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

    public void download(Integer file_id, HttpSession session, HttpServletResponse response){
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

    @Override
    public Map<Object, Object> create(Map<Object, Object> map, HttpSession session) {
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

    @Override
    public Map<Object, Object> delete(Map<Object, Object> map, HttpSession session) {
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

    @Override
    public Map<Object, Object> search(Map<Object, Object> map, HttpSession session) {
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

    @Override
    public Map<Object, Object> rename(List<Integer> operationList, String rename, Integer parent_id, HttpSession session) {
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

    @Override
    public Map<Object, Object> restore(List<Integer> operationList, HttpSession session) {
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

    @Override
    public void downloadFiles(List<Integer> operationList, HttpSession session, HttpServletResponse response) {
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

    @Override
    public Map<Object, Object> move(Map<Object, Object> map, HttpSession session) {
        Integer user_id = (Integer) session.getAttribute("user_id");
        Map<Object, Object> result = new HashMap<>();
        try {
            List<Integer> operationList = (List<Integer>) map.get("operationList");
            Integer parent_id = (Integer) map.get("parent_id");
            File parent_obj;
            List<File> parent_obj_list;
            List<File> obj_list;
            LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper<File> fileLambdaQueryWrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper<File> objLambdaQueryWrapper = new LambdaQueryWrapper<>();
            if(parent_id == 0){
                queryWrapper.isNotNull(File::getId);
                fileLambdaQueryWrapper.isNull(File::getId);
                objLambdaQueryWrapper.isNull(File::getParent_id);
            }else {
                queryWrapper.ne(File::getId, parent_id);
                fileLambdaQueryWrapper.eq(File::getId, parent_id);
                objLambdaQueryWrapper.eq(File::getParent_id, parent_id);
            }
            fileLambdaQueryWrapper.eq(File::getUser_id, user_id);
            fileLambdaQueryWrapper.eq(File::getIs_delete, 0);
            objLambdaQueryWrapper.eq(File::getUser_id, user_id);
            objLambdaQueryWrapper.eq(File::getIs_delete, 0);
            obj_list = fileMapper.selectList(objLambdaQueryWrapper);
            parent_obj_list = fileMapper.selectList(fileLambdaQueryWrapper);
            queryWrapper.eq(File::getUser_id, user_id);
            queryWrapper.eq(File::getIs_delete, 0);
            queryWrapper.in(File::getId, operationList);
            List<File> files = fileMapper.selectList(queryWrapper);
            if(files.size() > 0){
                for (File file : files) {
                    for (File file1 : obj_list) {
                        if (file1.getFilename().equals(file.getFilename()) && file1.getFiletype().equals(file.getFiletype())){
                            result.put("code", 1);
                            result.put("message", "该文件夹下存在相同名");
                            return result;
                        }
                    }
                    if(file.getParent_id() == null && parent_obj_list.size() > 0){
                        parent_obj = parent_obj_list.get(0);
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
                    if(parent_id != 0){
                        file.setParent_id(parent_id);
                        fileMapper.updateById(file);
                    }
                    else {
                        LambdaUpdateWrapper<File> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.set(File::getParent_id, null);
                        updateWrapper.in(File::getId, operationList);
                        fileMapper.update(file, updateWrapper);
                    }
                }
            }else {
                result.put("code", 1);
                result.put("message", "不可以移动到自己里面");
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("message", "移动失败");
            return result;
        }
        result.put("code", 0);
        result.put("message", "移动成功");
        return result;
    }

    @Override
    public Map<Object, Object> dirlist(Map<String, Integer> map, HttpSession session) {
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

    @Override
    public Map<Object, Object> dragMove(List<Integer> operationList, Integer parent_id, HttpSession session) {
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
        try{
            File parent_obj;
            List<File> parent_obj_list;
            List<File> obj_list;
            LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper<File> fileLambdaQueryWrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper<File> objLambdaQueryWrapper = new LambdaQueryWrapper<>();
            if(parent_id == 0){
                queryWrapper.isNotNull(File::getId);
                fileLambdaQueryWrapper.isNull(File::getId);
                objLambdaQueryWrapper.isNull(File::getParent_id);
            }else {
                queryWrapper.ne(File::getId, parent_id);
                fileLambdaQueryWrapper.eq(File::getId, parent_id);
                objLambdaQueryWrapper.eq(File::getParent_id, parent_id);
            }
            fileLambdaQueryWrapper.eq(File::getUser_id, user_id);
            fileLambdaQueryWrapper.eq(File::getIs_delete, 0);
            objLambdaQueryWrapper.eq(File::getUser_id, user_id);
            objLambdaQueryWrapper.eq(File::getIs_delete, 0);
            obj_list = fileMapper.selectList(objLambdaQueryWrapper);
            parent_obj_list = fileMapper.selectList(fileLambdaQueryWrapper);
            queryWrapper.eq(File::getUser_id, user_id);
            queryWrapper.eq(File::getIs_delete, 0);
            queryWrapper.in(File::getId, operationList);
            List<File> files = fileMapper.selectList(queryWrapper);
            if(files.size() > 0){
                for (File file : files) {
                    for (File file1 : obj_list) {
                        if (file1.getFilename().equals(file.getFilename()) && file1.getFiletype().equals(file.getFiletype())){
                            result.put("code", 1);
                            result.put("message", "该文件夹下存在相同名");
                            return result;
                        }
                    }
                    if(file.getParent_id() == null && parent_obj_list.size() > 0){
                        parent_obj = parent_obj_list.get(0);
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
                    if(parent_id != 0){
                        file.setParent_id(parent_id);
                        fileMapper.updateById(file);
                    }
                    else {
                        LambdaUpdateWrapper<File> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.set(File::getParent_id, null);
                        updateWrapper.in(File::getId, operationList);
                        fileMapper.update(file, updateWrapper);
                    }
                }
            }else {
                result.put("code", 1);
                result.put("message", "不可以移动到自己里面");
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 1);
            result.put("message", "移动失败");
            return result;
        }
        result.put("code", 0);
        result.put("message", "移动成功");
        return result;
    }

    @Override
    public Map<Object, Object> upload_already(String HASH, String filename, String file_hash, Integer parent_id, HttpSession session) {
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

    @Override
    public Map<Object, Object> upload(MultipartFile file, String hash, String file_hash, Integer parent_id, HttpSession session) {
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

    @Override
    public Map<Object, Object> uploadChunk(Map<String, MultipartFile> map) {
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

    @Override
    public Map<Object, Object> uploadMerge(String HASH, Integer count, Integer parent_id, String filename, String file_hash, HttpSession session) {
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
}
