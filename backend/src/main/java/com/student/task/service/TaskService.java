package com.student.task.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.task.dto.TaskDTO;
import com.student.task.entity.Task;
import com.student.task.entity.User;
import com.student.task.mapper.TaskMapper;

/**
 * 任务服务类
 */
@Service
public class TaskService extends ServiceImpl<TaskMapper, Task> {
    
    @Autowired
    private UserService userService;
    
    /**
     * 批量添加任务
     */
    public void batchAddTask(Long userId, TaskDTO taskDTO) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        List<Task> tasks = new ArrayList<>();
        LocalDate currentDate = taskDTO.getStartDate();
        LocalDate endDate = taskDTO.getEndDate() != null ? taskDTO.getEndDate() : taskDTO.getStartDate();
        
        while (!currentDate.isAfter(endDate)) {
            if (shouldCreateTask(currentDate, taskDTO.getRepeatType())) {
                Task task = new Task();
                BeanUtils.copyProperties(taskDTO, task);
                
                task.setUserId(userId);
                task.setTitle(extractTitle(taskDTO.getContent()));
                task.setTaskDate(currentDate);
                task.setStatus("pending");
                
                tasks.add(task);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        if (!tasks.isEmpty()) {
            this.saveBatch(tasks);
        }
    }
    
    /**
     * 判断是否应该创建任务
     */
    private boolean shouldCreateTask(LocalDate date, String repeatType) {
        switch (repeatType) {
            case "only_today":
                return true;
            case "everyday":
                return true;
            case "every_week_day":
                return true; // 每周的这一天
            case "monday_to_friday":
                DayOfWeek dayOfWeek = date.getDayOfWeek();
                return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
            case "custom":
                return true; // 自定义需要额外处理
            default:
                return false;
        }
    }
    
    /**
     * 提取任务标题
     */
    private String extractTitle(String content) {
        if (content == null || content.isEmpty()) {
            return "未命名任务";
        }
        String[] lines = content.split("\n");
        return lines[0].length() > 20 ? lines[0].substring(0, 20) + "..." : lines[0];
    }
    
    /**
     * 获取用户任务列表
     */
    public IPage<Task> getUserTaskList(Long userId, Integer page, Integer size, LocalDate date) {
        Page<Task> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId);
        
        if (date != null) {
            wrapper.eq(Task::getTaskDate, date);
        }
        
        wrapper.orderByDesc(Task::getTaskDate, Task::getCreateTime);
        
        return this.page(pageParam, wrapper);
    }
    
    /**
     * 开始任务
     */
    public void startTask(Long taskId, Long userId) {
        Task task = this.getById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在");
        }
        
        if (!"pending".equals(task.getStatus())) {
            throw new RuntimeException("任务状态不正确");
        }
        
        task.setStatus("in_progress");
        task.setActualStartTime(LocalDateTime.now());
        this.updateById(task);
    }
    
    /**
     * 完成任务
     */
    public void completeTask(Long taskId, Long userId) {
        Task task = this.getById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在");
        }
        
        task.setStatus("completed");
        task.setActualEndTime(LocalDateTime.now());
        
        // 计算耗时
        if (task.getActualStartTime() != null) {
            long seconds = java.time.Duration.between(task.getActualStartTime(), task.getActualEndTime()).getSeconds();
            task.setDuration((int) seconds);
        }
        
        this.updateById(task);
    }
    
    /**
     * 取消任务
     */
    public void cancelTask(Long taskId, Long userId) {
        Task task = this.getById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new RuntimeException("任务不存在");
        }
        
        task.setStatus("cancelled");
        this.updateById(task);
    }
    
    /**
     * 获取任务统计
     */
    public Map<String, Object> getTaskStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        return this.baseMapper.getTaskStatistics(userId, startDate, endDate);
    }
    
    /**
     * 获取分类统计
     */
    public List<Map<String, Object>> getCategoryStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        return this.baseMapper.getCategoryStatistics(userId, startDate, endDate);
    }
}
