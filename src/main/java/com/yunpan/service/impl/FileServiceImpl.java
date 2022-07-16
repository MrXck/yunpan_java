package com.yunpan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunpan.entity.File;
import com.yunpan.mapper.FileMapper;
import com.yunpan.service.FileService;
import org.springframework.stereotype.Service;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {
}
