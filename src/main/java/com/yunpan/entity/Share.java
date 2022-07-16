package com.yunpan.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("web_share")
public class Share {

    private Integer id;

    @TableField(value = "create_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime create_time;

    @TableField(value = "period")
    private Integer period;

    @TableField("password")
    private String password;

    @TableField("creator_id")
    private Integer creator_id;
}
