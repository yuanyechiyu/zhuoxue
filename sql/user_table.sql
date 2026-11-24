USE zhuoxue; -- 后面的语句都在 M0 建好的库里执行 
 
CREATE TABLE IF NOT EXISTS t_user ( -- 建学生用户表 
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户编号', -- 主键，自动递增 
  phone VARCHAR(20) DEFAULT NULL COMMENT '手机号，11 位', -- 登录用的电话号码 
  password_hash VARCHAR(100) DEFAULT NULL COMMENT 'BCrypt 密码指纹', -- 只存指纹不存明文 
  nickname VARCHAR(64) DEFAULT NULL COMMENT '昵称', -- 页面上显示的名字 
  avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像地址', -- 头像链接
  status TINYINT NOT NULL DEFAULT 1 COMMENT '0 禁用 1 正常', -- 账号状态 
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', -- 自动填注册时间 
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', -- 改资料时自动刷新 
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',    -- 0 正常，1 已删除（数据不真正抹掉）
  PRIMARY KEY (id), -- 主键 
  UNIQUE KEY uk_phone (phone) -- 手机号不许重复 
) ENGINE=InnoDB COMMENT='学生用户'; -- 存储引擎与表说明 