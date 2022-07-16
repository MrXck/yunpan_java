package com.yunpan.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("web_file")
public class File {

    private Integer id;

    @TableField("filename")
    private String filename;

    @TableField("file_hash_name")
    private String file_hash_name;

    @TableField("filetype")
    private Integer filetype;

    @TableField("filepath")
    private String filePath;

    @TableField("file_hash")
    private String file_hash;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime create_time;

    @TableField("is_delete")
    private Integer is_delete;

    @TableField("parent_id")
    private Integer parent_id;

    @TableField("user_id")
    private Integer user_id;
}
