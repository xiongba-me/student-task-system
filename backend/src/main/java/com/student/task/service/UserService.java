package com.student.task.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.task.dto.LoginDTO;
import com.student.task.dto.RegisterDTO;
import com.student.task.entity.User;
import com.student.task.mapper.UserMapper;
import com.student.task.util.JwtUtil;

/**
 * 用户服务类
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = this.getOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证密码
        String encryptPassword = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!encryptPassword.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 返回用户信息和Token
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", getUserInfo(user));
        
        return result;
    }
    
    /**
     * 用户注册
     */
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查学号是否已存在
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStudentNo, registerDTO.getStudentNo());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("学号已存在");
        }
        
        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        
        // 密码加密
        user.setPassword(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes()));
        user.setRole("student");
        user.setStatus(1);
        
        this.save(user);
    }
    
    /**
     * 获取用户信息
     */
    public Map<String, Object> getUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("studentNo", user.getStudentNo());
        userInfo.put("className", user.getClassName());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        userInfo.put("avatar", user.getAvatar());
        return userInfo;
    }
    
    /**
     * 根据ID获取用户信息
     */
    public Map<String, Object> getUserInfoById(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return getUserInfo(user);
    }
}
