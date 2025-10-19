package com.student.task.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginDTO {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
}
