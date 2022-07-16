package com.yunpan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunpan.entity.ShareFiles;
import com.yunpan.mapper.ShareFilesMapper;
import com.yunpan.service.ShareFilesService;
import org.springframework.stereotype.Service;

@Service
public class ShareFilesServiceImpl extends ServiceImpl<ShareFilesMapper, ShareFiles> implements ShareFilesService {
}
