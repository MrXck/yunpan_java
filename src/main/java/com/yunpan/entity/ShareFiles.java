package com.yunpan.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("web_share_files")
public class ShareFiles {

    private Integer id;

    @TableField("share_id")
    private Integer share_id;

    @TableField("file_id")
    private Integer file_id;

}
