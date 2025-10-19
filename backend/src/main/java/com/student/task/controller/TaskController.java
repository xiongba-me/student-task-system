package com.student.task.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.task.dto.TaskDTO;
import com.student.task.entity.Task;
import com.student.task.service.TaskService;
import com.student.task.util.Result;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/task")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    /**
     * 批量添加任务
     */
    @PostMapping("/batch-add")
    public Result<String> batchAddTask(@Validated @RequestBody TaskDTO taskDTO,
                                        HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            taskService.batchAddTask(userId, taskDTO);
            return Result.success("任务添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取任务列表
     */
    @GetMapping("/list")
    public Result<IPage<Task>> getTaskList(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "20") Integer size,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            IPage<Task> result = taskService.getUserTaskList(userId, page, size, date);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 开始任务
     */
    @PostMapping("/start/{id}")
    public Result<String> startTask(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            taskService.startTask(id, userId);
            return Result.success("任务已开始");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 完成任务
     */
    @PostMapping("/complete/{id}")
    public Result<String> completeTask(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            taskService.completeTask(id, userId);
            return Result.success("任务已完成");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消任务
     */
    @PostMapping("/cancel/{id}")
    public Result<String> cancelTask(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            taskService.cancelTask(id, userId);
            return Result.success("任务已取消");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteTask(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Task task = taskService.getById(id);
            if (task == null || !task.getUserId().equals(userId)) {
                return Result.error("任务不存在");
            }
            taskService.removeById(id);
            return Result.success("任务已删除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取任务统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> statistics = taskService.getTaskStatistics(userId, startDate, endDate);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取分类统计
     */
    @GetMapping("/category-statistics")
    public Result<List<Map<String, Object>>> getCategoryStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Map<String, Object>> statistics = taskService.getCategoryStatistics(userId, startDate, endDate);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
