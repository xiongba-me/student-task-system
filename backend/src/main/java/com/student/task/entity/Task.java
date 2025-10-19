package com.student.task.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 任务实体类
 */
@Data
@TableName("task")
public class Task implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务内容
     */
    private String content;

    /**
     * 任务分类（运动、英语、语文、数学等）
     */
    private String category;

    /**
     * 任务颜色标识
     */
    private String color;

    /**
     * 重复频率：only_today-仅当天, everyday-每天, every_week_day-每周的这天, monday_to_friday-周一至周五, custom-自定义
     */
    private String repeatType;

    /**
     * 自定义重复配置（JSON格式，存储具体哪些日期）
     */
    private String repeatConfig;

    /**
     * 计划开始时间
     */
    private LocalTime planStartTime;

    /**
     * 计划结束时间
     */
    private LocalTime planEndTime;

    /**
     * 实际开始时间
     */
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    private LocalDateTime actualEndTime;

    /**
     * 任务状态：pending-待完成, in_progress-进行中, completed-已完成, cancelled-已取消
     */
    private String status;

    /**
     * 任务日期
     */
    private LocalDate taskDate;

    /**
     * 起始日期（重复任务的开始日期）
     */
    private LocalDate startDate;

    /**
     * 结束日期（重复任务的结束日期）
     */
    private LocalDate endDate;

    /**
     * 实际耗时（秒）
     */
    private Integer duration;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
