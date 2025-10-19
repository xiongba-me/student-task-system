package com.student.task.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterDTO {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    @NotBlank(message = "学号不能为空")
    private String studentNo;
    
    @NotBlank(message = "班级不能为空")
    private String className;
    
    private String phone;
    
    private String email;
}
