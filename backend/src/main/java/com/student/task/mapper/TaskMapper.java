package com.student.task.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.task.entity.Task;

/**
 * 任务Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    
    /**
     * 获取用户任务统计
     */
    Map<String, Object> getTaskStatistics(@Param("userId") Long userId, 
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * 获取分类统计
     */
    List<Map<String, Object>> getCategoryStatistics(@Param("userId") Long userId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
}
