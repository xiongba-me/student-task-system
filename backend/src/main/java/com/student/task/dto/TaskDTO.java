package com.student.task.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 任务请求DTO
 */
@Data
public class TaskDTO {
    
    @NotBlank(message = "任务内容不能为空")
    private String content;
    
    private String category;
    
    private String color;
    
    @NotBlank(message = "重复频率不能为空")
    private String repeatType;
    
    private String repeatConfig;
    
    private LocalTime planStartTime;
    
    private LocalTime planEndTime;
    
    @NotNull(message = "起始日期不能为空")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String remark;
}
