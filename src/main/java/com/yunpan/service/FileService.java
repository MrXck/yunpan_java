package com.yunpan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunpan.entity.File;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

public interface FileService extends IService<File> {
    Map<Object, Object> file(Map<Object, Integer> map, HttpSession session);

    void download(Integer file_id, HttpSession session, HttpServletResponse response);

    Map<Object, Object> create(Map<Object, Object> map, HttpSession session);

    Map<Object, Object> delete(Map<Object, Object> map, HttpSession session);

    Map<Object, Object> search(Map<Object, Object> map, HttpSession session);

    Map<Object, Object> rename(List<Integer> operationList, String rename, Integer parent_id, HttpSession session);

    Map<Object, Object> restore(List<Integer> operationList, HttpSession session);

    void downloadFiles(List<Integer> operationList, HttpSession session, HttpServletResponse response);

    Map<Object, Object> move(Map<Object, Object> map, HttpSession session);

    Map<Object, Object> dirlist(Map<String, Integer> map, HttpSession session);

    Map<Object, Object> dragMove(List<Integer> operationList, Integer parent_id, HttpSession session);

    Map<Object, Object> upload_already(String HASH, String filename, String file_hash, Integer parent_id, HttpSession session);

    Map<Object, Object> upload(MultipartFile file, String hash, String file_hash, Integer parent_id, HttpSession session);

    Map<Object, Object> uploadChunk(Map<String, MultipartFile> map);

    Map<Object, Object> uploadMerge(String HASH, Integer count, Integer parent_id, String filename, String file_hash, HttpSession session);
}
