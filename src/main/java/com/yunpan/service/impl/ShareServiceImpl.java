package com.yunpan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunpan.entity.Share;
import com.yunpan.mapper.ShareMapper;
import com.yunpan.service.ShareService;
import org.springframework.stereotype.Service;

@Service
public class ShareServiceImpl extends ServiceImpl<ShareMapper, Share> implements ShareService {
}
