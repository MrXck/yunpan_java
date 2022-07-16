package com.yunpan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("web_user")
@Data
public class User {

    private Integer id;

    private String username;

    private String password;
}
