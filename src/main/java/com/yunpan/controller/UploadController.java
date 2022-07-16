package com.yunpan.controller;

import com.yunpan.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private FileService fileService;


    @GetMapping("/upload_already")
    public Map<Object, Object> upload_already(String HASH, String filename, String file_hash, Integer parent_id, HttpSession session){
        return fileService.upload_already(HASH, filename, file_hash, parent_id, session);
    }

    @PostMapping("/upload")
    public Map<Object, Object> upload(MultipartFile file, String hash, String file_hash, Integer parent_id, HttpSession session){
        return fileService.upload(file, hash, file_hash, parent_id, session);
    }

    @PostMapping("/upload_chunk")
    public Map<Object, Object> uploadChunk(@RequestParam Map<String, MultipartFile> map){
        return fileService.uploadChunk(map);
    }

    @GetMapping("/upload_merge")
    public Map<Object, Object> uploadMerge(String HASH, Integer count, Integer parent_id, String filename, String file_hash, HttpSession session){
        return fileService.uploadMerge(HASH, count, parent_id, filename, file_hash, session);
    }


}
