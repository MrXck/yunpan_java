package com.yunpan.controller;

import com.yunpan.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/file")
    public Map<Object, Object> file(@RequestBody Map<Object, Integer> map, HttpSession session){
        return fileService.file(map, session);
    }

    @GetMapping("/download/{file_id}")
    public void download(@PathVariable("file_id")Integer file_id, HttpSession session, HttpServletResponse response){
        fileService.download(file_id, session, response);
    }

    @PostMapping("/create")
    public Map<Object, Object> create(@RequestBody Map<Object, Object> map, HttpSession session){
        return fileService.create(map, session);
    }

    @PostMapping("/delete")
    public Map<Object, Object> delete(@RequestBody Map<Object, Object> map, HttpSession session){
        return fileService.delete(map, session);
    }

    @PostMapping("/search")
    public Map<Object, Object> search(@RequestBody Map<Object, Object> map, HttpSession session){
        return fileService.search(map, session);
    }

    @GetMapping("/rename")
    public Map<Object, Object> rename(@RequestParam("operationList") List<Integer> operationList, String rename, Integer parent_id, HttpSession session){
        return fileService.rename(operationList, rename, parent_id, session);
    }

    @GetMapping("/restore_files")
    public Map<Object, Object> restore(@RequestParam("operationList") List<Integer> operationList, HttpSession session){
        return fileService.restore(operationList, session);
    }

    @GetMapping("/download_files")
    public void downloadFiles(@RequestParam("operationList") List<Integer> operationList, HttpSession session, HttpServletResponse response) {
        fileService.downloadFiles(operationList, session, response);
    }

}



