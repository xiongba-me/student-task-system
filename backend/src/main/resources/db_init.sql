-- 创建数据库
CREATE DATABASE IF NOT EXISTS study DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE study;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `student_no` VARCHAR(20) NOT NULL COMMENT '学号',
  `class_name` VARCHAR(50) NOT NULL COMMENT '班级',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `role` VARCHAR(20) NOT NULL DEFAULT 'student' COMMENT '角色：student-学生, admin-管理员',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  KEY `idx_class_name` (`class_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- 插入测试数据
-- 管理员账号：admin/admin123
INSERT INTO `user` (`username`, `password`, `real_name`, `student_no`, `class_name`, `phone`, `email`, `role`, `status`) 
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '管理员', 'ADMIN001', '管理员', '13800138000', 'admin@example.com', 'admin', 1);

-- 学生账号：student1/123456
INSERT INTO `user` (`username`, `password`, `real_name`, `student_no`, `class_name`, `phone`, `email`, `role`, `status`) 
VALUES ('student1', 'e10adc3949ba59abbe56e057f20f883e', '张三', '2024001', '计算机1班', '13800138001', 'zhangsan@example.com', 'student', 1);

INSERT INTO `user` (`username`, `password`, `real_name`, `student_no`, `class_name`, `phone`, `email`, `role`, `status`) 
VALUES ('student2', 'e10adc3949ba59abbe56e057f20f883e', '李四', '2024002', '计算机1班', '13800138002', 'lisi@example.com', 'student', 1);

INSERT INTO `user` (`username`, `password`, `real_name`, `student_no`, `class_name`, `phone`, `email`, `role`, `status`) 
VALUES ('student3', 'e10adc3949ba59abbe56e057f20f883e', '王五', '2024003', '计算机2班', '13800138003', 'wangwu@example.com', 'student', 1);

-- 任务表
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '任务标题',
  `content` TEXT COMMENT '任务内容',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '任务分类（运动、英语、语文、数学等）',
  `color` VARCHAR(20) DEFAULT NULL COMMENT '任务颜色标识',
  `repeat_type` VARCHAR(50) NOT NULL DEFAULT 'only_today' COMMENT '重复频率',
  `repeat_config` VARCHAR(500) DEFAULT NULL COMMENT '自定义重复配置（JSON）',
  `plan_start_time` TIME DEFAULT NULL COMMENT '计划开始时间',
  `plan_end_time` TIME DEFAULT NULL COMMENT '计划结束时间',
  `actual_start_time` DATETIME DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` DATETIME DEFAULT NULL COMMENT '实际结束时间',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '任务状态',
  `task_date` DATE NOT NULL COMMENT '任务日期',
  `start_date` DATE NOT NULL COMMENT '起始日期',
  `end_date` DATE DEFAULT NULL COMMENT '结束日期',
  `duration` INT DEFAULT NULL COMMENT '实际耗时（秒）',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_task_date` (`task_date`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 插入测试任务
INSERT INTO `task` (`user_id`, `title`, `content`, `category`, `color`, `repeat_type`, `plan_start_time`, `plan_end_time`, `status`, `task_date`, `start_date`, `end_date`) 
VALUES 
(2, '跳绳', '内容：跳绳打卡', '运动', 'purple', 'everyday', '21:48:00', '21:52:00', 'completed', CURDATE(), CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY)),
(2, '听读英语课文', '内容：听英语，跟读课文', '英语', 'blue', 'everyday', '20:27:00', '20:41:00', 'completed', CURDATE(), CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY)),
(2, '常规作业1', '内容：完成《能培》《小狗学叫》', '语文', 'green', 'only_today', '19:44:00', '20:04:00', 'completed', CURDATE(), CURDATE(), CURDATE()),
(2, '听写', '内容：听写回地三词语P114', '语文', 'green', 'only_today', '19:30:00', '19:33:00', 'pending', CURDATE(), CURDATE(), CURDATE());
